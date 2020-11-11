package com.sdm.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.sdm.Constants;
import com.sdm.admin.model.Role;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.*;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.FBGraphManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.GoogleApiManager;
import com.sdm.file.model.File;
import com.sdm.file.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Service
@Log4j2
public class AuthService {

    @Autowired
    private FBGraphManager facebookGraphManager;

    @Autowired
    private GoogleApiManager googleApiManager;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthMailService mailService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HttpSession session;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    FileService fileService;

    private static final String FB_AUTH_FIELDS = "id,name,email,picture{url},gender";

    private static final int MAX_PASSWORD = 32;
    private static final int MIN_PASSWORD = 16;

    private int increaseFailedCount() {
        Integer count = (Integer) session.getAttribute(Constants.SESSION.AUTH_FAILED_COUNT);
        if (count == null) {
            count = 0;
        }
        count++;
        session.setAttribute(Constants.SESSION.AUTH_FAILED_COUNT, count);
        return count;
    }

    private String getActivateCallbackURL() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/activate").toUriString();
    }

    private void setAnonymousExtras(AnonymousRequest request, User user) {
        if (!StringUtils.isEmpty(request.getBrand())) {
            user.addExtra("brand", request.getBrand());
        }

        if (!StringUtils.isEmpty(request.getCarrier())) {
            user.addExtra("carrier", request.getCarrier());
        }

        if (!StringUtils.isEmpty(request.getManufacture())) {
            user.addExtra("manufacture", request.getManufacture());
        }
    }

    private User createAnonymousUser(AnonymousRequest request) {
        Random rnd = new Random();
        int size = rnd.nextInt((MAX_PASSWORD - MIN_PASSWORD) + 1) + MIN_PASSWORD;

        String userName = request.getDeviceOS() + "_"
                + Globalizer.getDateString("yyyyMMddHHmmss", new Date()) + "_"
                + Globalizer.generateToken(Constants.Auth.GENERATED_TOKEN_CHARS, 8);

        String passwordChars = securityManager.randomPassword(25);
        String rawPassword = Globalizer.generateToken(passwordChars, size);
        String password = securityManager.hashString(rawPassword);
        return new User(userName, "Anonymous", password, User.Status.ACTIVE);
    }

    @Transactional
    public ResponseEntity<MessageResponse> accountActivation(ActivateRequest request) {
        User user = userRepository.checkOTP(request.getUser(), request.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "Your OTP is invalid. Pls try to contact admin team."));

        //Resend OTP to User
        if (user.getOtpExpired().before(new Date())) {
            try {
                this.mailService.activateLink(user, getActivateCallbackURL());
            } catch (JsonProcessingException ex) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
            }
            userRepository.save(user);
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                    "Sorry! Your token has expired. We send new token to your email.");
        }

        user.setOtpToken(null);
        user.setOtpExpired(null);

        if (securityManager.getProperties().isRequireConfirm()) {
            user.setStatus(User.Status.ACTIVE);
        }
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "activation_success", "Your account is ready.", null));
    }

    public ResponseEntity<User> resetPasswordByOtp(ChangePasswordRequest changePasswordRequest, ActivateRequest activateRequest) {
        User user = userRepository.checkOTP(activateRequest.getUser(), activateRequest.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "Your OTP is invalid. Pls try to contact admin team."));

        if (user.getOtpExpired().before(new Date()) || !user.getOtpToken().equals(activateRequest.getToken())) {
            user.setOtpToken(null);
            user.setOtpExpired(null);
            userRepository.save(user);
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                    "Your OTP is invalid. Pls try to contact admin team.");
        }

        String newPassword = securityManager.hashString(changePasswordRequest.getNewPassword());
        user.setPassword(newPassword);
        user.setOtpToken(null);
        user.setOtpExpired(null);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    public ResponseEntity<MessageResponse> forgetPassword(ForgetPasswordRequest request) {
        User user = userRepository.findFirstByPhoneNumberAndEmail(request.getPhoneNumber(), request.getEmail())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Invalid phone number (or) email address."));
        try {
            mailService.forgetPasswordLink(user, request.getCallback());
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "send_otp", "We send the reset password link to your e-mail.", null));
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Transactional
    public ResponseEntity<User> authByPassword(AuthRequest request) {
        String password = securityManager.hashString(request.getPassword());
        User authUser = userRepository.authByPassword(request.getUser(), password)
                .orElseThrow(() -> {
                    increaseFailedCount();
                    return new GeneralException(HttpStatus.UNAUTHORIZED,
                            "Opp! request email or password is something wrong");
                });

        jwtService.createToken(authUser, request, httpServletRequest);

        return ResponseEntity.ok(authUser);
    }

    @Transactional
    public ResponseEntity<User> registerByUserAndEmail(RegistrationRequest request) {
        //Check user by user name
        userRepository.findFirstByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail())
                .ifPresent(user -> {
                    if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this email.");
                    } else if (user.getPhoneNumber().equalsIgnoreCase(request.getPhoneNumber())) {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this phone number.");
                    }
                });

        boolean needConfirm = securityManager.getProperties().isRequireConfirm();
        User.Status status = needConfirm ? User.Status.PENDING : User.Status.ACTIVE;
        String password = securityManager.hashString(request.getPassword());
        User newUser = new User(request.getEmail(), request.getPhoneNumber(), request.getDisplayName(),
                password, status);
        userRepository.save(newUser);
        if (needConfirm) {
            try {
                mailService.activateLink(newUser, getActivateCallbackURL());
            } catch (JsonProcessingException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        } else {
            jwtService.createToken(newUser, request, httpServletRequest);
        }

        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    public File createGoogleImage(String pictureUrl){
        if(pictureUrl!=null){
            try {
                return fileService.create(pictureUrl,false);
            } catch (IOException e) {
                log.error("GOOGLE_IMAGE_FAIL>>>"+e.getLocalizedMessage());
            }
        }
        return null;
    }

    public User createGoogleUser(Map<String,Object> profileObj){
        Optional<User> dbEntity;
        User userEntity;

        String phoneNumber="GL_"+profileObj.get("userId");
        String email=(String) profileObj.get("email");
        String displayName=(String)profileObj.get("name");
        String pictureUrl=(String)profileObj.get("pictureUrl");
        String gender="";
        File profilePicture=this.createGoogleImage(pictureUrl);

        Random rnd = new Random();
        int size = rnd.nextInt((MAX_PASSWORD - MIN_PASSWORD) + 1) + MIN_PASSWORD;
        String passwordChars = securityManager.randomPassword(25);
        String rawPassword = Globalizer.generateToken(passwordChars, size);
        String password = securityManager.hashString(rawPassword);

        //Get Back Old User Data With Email
        dbEntity = userRepository.findFirstByPhoneNumberOrEmail(phoneNumber, email);
        Map<String,String> extra=new HashMap<>();

        if (dbEntity.isPresent()) {
            userEntity = dbEntity.get();
            userEntity.setDisplayName(displayName);
        } else {
            userEntity = new User(phoneNumber, displayName, password, User.Status.ACTIVE);
            userEntity.setEmail(email);
        }
        if(profilePicture!=null){
            userEntity.setProfileImage(profilePicture);
        }
        userEntity.setGoogleId((String)profileObj.get("userId"));

        Integer clientRole=securityManager.getProperties().getClientRole();
        Role role =roleRepository.findById(clientRole).orElseThrow(() -> new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error Creating User Role."));
        Set<Role> newRole=new HashSet<>();
        newRole.add(role);
        userEntity.setRoles(newRole);

        return userRepository.save(userEntity);
    }

    public File createFacebookImage(JsonObject profileObj){
        if(profileObj.has("picture")){
            JsonObject pictureObj=profileObj.getAsJsonObject("picture");
            if(pictureObj.has("data")){
                JsonObject pictureDataObj=pictureObj.getAsJsonObject("data");
                if(pictureDataObj.has("url")){
                    String pictureUrl=pictureDataObj.get("url").getAsString();
                    try {
                        return fileService.create(pictureUrl,false);
                    } catch (IOException e) {
                        log.error("FACEBOOK_IMAGE_FAIL>>>"+e.getLocalizedMessage());
                    }
                }
            }
        }

        return null;
    }

    public User createFacebookUser(JsonObject profileObj){
        Optional<User> dbEntity;
        User userEntity;

        String phoneNumber = "FB_" + profileObj.get("id").getAsString();
        String email = "fb" + profileObj.get("id").getAsString() + "@facebook.com";
        String displayName = profileObj.get("name").getAsString();
        String gender="";
        File profilePicture=this.createFacebookImage(profileObj);;

        Random rnd = new Random();
        int size = rnd.nextInt((MAX_PASSWORD - MIN_PASSWORD) + 1) + MIN_PASSWORD;
        String passwordChars = securityManager.randomPassword(25);
        String rawPassword = Globalizer.generateToken(passwordChars, size);
        String password = securityManager.hashString(rawPassword);

        if (profileObj.has("email")) {
            email=profileObj.get("email").getAsString();
        }

        if (profileObj.has("phone")) {
            phoneNumber = profileObj.get("phone").getAsString();
        }

        if (profileObj.has("gender")) {
            gender = profileObj.get("gender").getAsString();
        }

        //Get Back Old User Data With Email
        dbEntity = userRepository.findFirstByPhoneNumberOrEmail(phoneNumber, email);
        Map<String,String> extra=new HashMap<>();

        if (dbEntity.isPresent()) {
            userEntity = dbEntity.get();
            userEntity.setDisplayName(displayName);
            extra=userEntity.getExtras();
            extra.put("gender",gender);
            userEntity.setExtras(extra);
        } else {
            userEntity = new User(phoneNumber, displayName, password, User.Status.ACTIVE);
            userEntity.setEmail(email);
            extra.put("gender",gender);
            userEntity.setExtras(extra);
        }
        if(profilePicture!=null){
            userEntity.setProfileImage(profilePicture);
        }
        userEntity.setFacebookId(profileObj.get("id").getAsString());

        //Set Default Role
        Integer clientRole=securityManager.getProperties().getClientRole();
        if(clientRole!=null){
            Role role =roleRepository.findById(clientRole).orElseThrow(() -> new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error Creating User Role."));
            Set<Role> newRole=new HashSet<>();
            newRole.add(role);
            userEntity.setRoles(newRole);
        }

        return userRepository.save(userEntity);
    }

    @Transactional
    public ResponseEntity<User> facebookAuth(OAuth2Request request) throws IOException {
        JsonObject facebookProfile = facebookGraphManager.checkFacebookToken(request.getAccessToken(), FB_AUTH_FIELDS);
        String id = facebookProfile.get("id").getAsString();

        //Check User by FacebookId
        User authUser = userRepository.findFirstByFacebookId(id)
                .orElseGet(() -> this.createFacebookUser(facebookProfile));

        if (authUser.getFacebookId().equalsIgnoreCase(facebookProfile.get("id").getAsString())) {
            jwtService.createToken(authUser, request, httpServletRequest);
        } else {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid Access Token!");
        }

        return ResponseEntity.ok(authUser);
    }

    public ResponseEntity<User> googleAuth(OAuth2Request request)throws IOException{
        Map<String,Object> googleProfile=googleApiManager.checkGoogle(request.getAccessToken());
        String email=(String)googleProfile.get("email");

        User authUser=userRepository.findFirstByPhoneNumberOrEmail("",email)
                .orElseGet(()->this.createGoogleUser(googleProfile));

        if(authUser.getGoogleId()==null){
            authUser.setGoogleId((String)googleProfile.get("userId"));
            File profilePicture=this.createGoogleImage((String)googleProfile.get("pictureUrl"));

            if(profilePicture!=null)
                authUser.setProfileImage(profilePicture);
            authUser=userRepository.save(authUser);
        }

        if (authUser.getGoogleId().equalsIgnoreCase((String)googleProfile.get("userId"))) {
            jwtService.createToken(authUser, request, httpServletRequest);
        } else {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid Access Token!");
        }

        return ResponseEntity.ok(authUser);
    }
}
