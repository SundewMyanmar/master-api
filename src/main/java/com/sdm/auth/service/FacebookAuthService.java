package com.sdm.auth.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sdm.admin.model.Role;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.OAuth2Request;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import com.sdm.storage.model.File;
import com.sdm.storage.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Service
@Log4j2
public class FacebookAuthService implements SocialAuthService {
    private static final String FB_GRAPH_URL = "https://graph.facebook.com/v9.0/";

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
    private PasswordEncoder passwordEncoder;

    private static final String FB_AUTH_FIELDS = "id,name,email,picture.width(512),gender";


    public JsonObject checkFacebookToken(String accessToken) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(FB_GRAPH_URL + "me")
                .queryParam("fields", FB_AUTH_FIELDS)
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
                        return fileService.create(pictureUrl, false, true, null);
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
