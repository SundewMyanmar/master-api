package com.sdm.auth.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sdm.admin.model.Role;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.config.properties.FacebookProperties;
import com.sdm.auth.model.request.OAuth2Request;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.annotation.FileClassification;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.core.util.SettingManager;
import com.sdm.storage.model.File;
import com.sdm.storage.service.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class FacebookAuthService implements SocialAuthService {

    @Autowired
    FileService fileService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SecurityManager securityManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpRequestManager requestManager;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private FacebookProperties getProperties(){
        FacebookProperties properties = new FacebookProperties();
        try {
            properties = settingManager.loadSetting(FacebookProperties.class);
        } catch (IOException | IllegalAccessException e) {
            log.error(e.getLocalizedMessage());
        }
        return properties;
    }

    public Map<String, String> facebookDataDeletion(String signedRequest) {
        log.info("Data deletion request from Facebook => " + signedRequest);
        String[] signedData = signedRequest.split("\\.", 2);
        if (signedData.length != 2) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid_facebook_sign"));
        }
        String signature = DatatypeConverter.printHexBinary(Base64.getUrlDecoder().decode(signedData[0]));

        String expectedSignature = securityManager.generateHashHmac(signedData[1], getProperties().getAppSecret(), "HmacSHA256");
        if (!signature.equals(expectedSignature)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid_facebook_sign"));
        }

        String confirmationCode = Globalizer.generateToken("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 6);
        String url = Globalizer.getCurrentContextPath("/facebook/deletion", true) + "?id=" + confirmationCode;
        return Map.of(
                "confirmation_code", confirmationCode,
                "url", url
        );
    }

    public JsonObject checkFacebookToken(String accessToken) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(getProperties().getGraphUrl() + "me")
                .queryParam("fields", getProperties().getAuthFields())
                .queryParam("access_token", accessToken);
        try {
            HttpResponse response = requestManager.jsonGetRequest(new URL(uriBuilder.toUriString()), "", true);
            if (response.getCode() != 200) {
                throw new GeneralException(HttpStatus.valueOf(response.getCode()),
                        localeManager.getMessage("invalid-facebook-account"));
            }

            return new Gson().fromJson(response.getBody(), JsonObject.class);
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Transactional
    public File createProfileImage(JsonObject profileObj) {
        if (profileObj.has("picture")) {
            JsonObject pictureObj = profileObj.getAsJsonObject("picture");
            if (pictureObj.has("data")) {
                JsonObject pictureDataObj = pictureObj.getAsJsonObject("data");
                if (pictureDataObj.has("url")) {
                    String pictureUrl = pictureDataObj.get("url").getAsString();
                    try {
                        Field profileImageField= null;
                        FileClassification annotation=null;
                        try {
                            profileImageField = User.class.getDeclaredField("profileImage");
                            annotation=profileImageField.getAnnotation(FileClassification.class);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                        return fileService.loadExternalImage(pictureUrl, annotation);
                    } catch (IOException e) {
                        log.error("FACEBOOK_IMAGE_FAIL >>>" + e.getLocalizedMessage());
                    }
                }
            }
        }

        return null;
    }

    @Transactional
    public User createUser(JsonObject profileObj) {
        Optional<User> dbEntity = Optional.empty();
        User userEntity;

        String phoneNumber = "FB_" + profileObj.get("id").getAsString();
        String email = "fb" + profileObj.get("id").getAsString() + "@facebook.com";
        String displayName = profileObj.get("name").getAsString();
        String gender = "";

        String rawPassword = Globalizer.randomPassword(AuthService.MIN_PASSWORD, AuthService.MAX_PASSWORD);
        String password = passwordEncoder.encode(rawPassword);

        if (profileObj.has("email")) {
            email = profileObj.get("email").getAsString();
        }

        if (profileObj.has("phone")) {
            phoneNumber = Globalizer.cleanPhoneNo(profileObj.get("phone").getAsString());
        }

        if (profileObj.has("gender")) {
            gender = profileObj.get("gender").getAsString();
        }
        File profilePicture = this.createProfileImage(profileObj);
        //Get Back Old User Data With Email
        if (!Globalizer.isNullOrEmpty(email) && !Globalizer.isNullOrEmpty(phoneNumber)) {
            dbEntity = userRepository.findFirstByPhoneNumberOrEmail(phoneNumber, email);
        } else if (!Globalizer.isNullOrEmpty(phoneNumber)) {
            dbEntity = userRepository.findFirstByPhoneNumberAndPhoneNumberIsNotNull(phoneNumber);
        } else if (!Globalizer.isNullOrEmpty(email)) {
            dbEntity = userRepository.findFirstByEmailAndEmailIsNotNull(email);
        }

        Map<String, String> extra = new HashMap<>();

        if (dbEntity.isPresent()) {
            userEntity = dbEntity.get();
            userEntity.setDisplayName(displayName);
            extra = userEntity.getExtras();
            extra.put("gender", gender);
            userEntity.setExtras(extra);
        } else {
            userEntity = new User(phoneNumber, displayName, password, User.Status.ACTIVE);
            userEntity.setEmail(email);
            extra.put("gender", gender);
            userEntity.setExtras(extra);
        }
        if (profilePicture != null) {
            userEntity.setProfileImage(profilePicture);
        }
        userEntity.setFacebookId(profileObj.get("id").getAsString());

        Integer clientRole = securityManager.getProperties().getClientRole();
        Role role = roleRepository.findById(clientRole).orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST,
                localeManager.getMessage("error-create-user-role")));
        Set<Role> newRole = new HashSet<>();
        newRole.add(role);
        userEntity.setRoles(newRole);

        return userRepository.save(userEntity);
    }

    @Override
    public ResponseEntity<User> auth(OAuth2Request request) {
        JsonObject facebookProfile = this.checkFacebookToken(request.getAccessToken());
        String id = facebookProfile.get("id").getAsString();

        //Check User by FacebookId
        User authUser = userRepository.findFirstByFacebookId(id)
                .orElseGet(() -> this.createUser(facebookProfile));

        if (authUser.getFacebookId().equalsIgnoreCase(facebookProfile.get("id").getAsString())) {
            jwtService.createToken(authUser, request, httpServletRequest);
        } else {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("facebook-linked-failed"));
        }
        return ResponseEntity.ok(authUser);
    }

    @Override
    public ResponseEntity<User> link(String accessId, User user) {
        //Check User by FacebookId
        User authUser = userRepository.findFirstByFacebookId(accessId)
                .orElseGet(() -> user);

        if (!authUser.getId().equals(user.getId())) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("facebook-already-linked"));
        } else {
            authUser.setFacebookId(null);
        }

        if (Globalizer.isNullOrEmpty(user.getFacebookId())) {
            user.setFacebookId(accessId);
        } else {
            user.setFacebookId(null);
        }

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

}
