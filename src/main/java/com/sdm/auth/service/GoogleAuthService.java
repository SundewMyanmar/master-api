package com.sdm.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.sdm.admin.model.Role;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.OAuth2Request;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.security.SecurityManager;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Service
@Log4j2
public class GoogleAuthService implements SocialAuthService {
    @Autowired
    FileService fileService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SecurityManager securityManager;

    @Autowired
    GoogleApiManager googleApiManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Transactional
    public File createProfileImage(String pictureUrl) {
        /*
            Sample Url
            Get Large 512 Image
            https://lh3.googleusercontent.com/a-/AOh14GgjXuK3-1PHnT89zU6gQXkxn1W_CjZt4AUBbo5t_g=s96-c
         */
        if (pictureUrl.contains("=s96-c"))
            pictureUrl = pictureUrl.replace("=s96-c", "=s512-c");

        if (pictureUrl != null) {
            try {
                return fileService.create(pictureUrl, false, true);
            } catch (IOException e) {
                log.error("GOOGLE_IMAGE_FAIL>>>" + e.getLocalizedMessage());
            }
        }
        return null;
    }

    /**
     * Get User Info from Google Payload
     *
     * @param payload GooglePayload
     */
    @Transactional
    private User createUser(GoogleIdToken.Payload payload) {
        String userId = payload.getSubject();
        String phoneNumber = "GL_" + userId;
        String email = payload.getEmail();
        String displayName = (String) payload.get("name");
        String rawPassword = Globalizer.randomPassword(AuthService.MIN_PASSWORD, AuthService.MAX_PASSWORD);
        String password = securityManager.hashString(rawPassword);
        File profilePicture = this.createProfileImage(payload.get("picture").toString());

        if (!payload.getEmailVerified()) {
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE, "You need to verify your e-mail account first.");
        }

        //Check User Data

        //Get Back Old User Data With Email
        User userEntity = userRepository.findFirstByPhoneNumberOrEmail(phoneNumber, email)
                .orElse(new User(email, phoneNumber, displayName, password, User.Status.ACTIVE));

        if (Globalizer.isNullOrEmpty(userEntity.getGoogleId())) {
            userEntity.setGoogleId(userId);
        }

        if (Globalizer.isNullOrEmpty(userEntity.getProfileImage())) {
            if (profilePicture != null) {
                userEntity.setProfileImage(profilePicture);
            }
        }

        Integer clientRole = securityManager.getProperties().getClientRole();
        Optional<Role> role = roleRepository.findById(clientRole);
        if (role.isPresent()) {
            userEntity.setRoles(Set.of(role.get()));
        }

        return userRepository.save(userEntity);
    }

    @Override
    public ResponseEntity<User> auth(OAuth2Request request) {
        final GoogleIdToken.Payload payload = googleApiManager.checkGoogle(request.getAccessToken());
        String email = payload.getEmail();

        User authUser = userRepository.findFirstByPhoneNumberOrEmail("", email)
                .orElseGet(() -> this.createUser(payload));

        if (Globalizer.isNullOrEmpty(authUser.getGoogleId())) {
            authUser.setGoogleId(payload.getSubject());
        }

        if (Globalizer.isNullOrEmpty(authUser.getProfileImage())) {
            String profilePic = (String) payload.get("picture");
            File profilePicture = this.createProfileImage(profilePic);
            if (profilePicture != null) {
                authUser.setProfileImage(profilePicture);
            }
        }

        if (authUser.getGoogleId().equalsIgnoreCase(payload.getSubject())) {
            jwtService.createToken(authUser, request, httpServletRequest);
        } else {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid Access Token!");
        }

        return ResponseEntity.ok(authUser);
    }

    @Override
    public ResponseEntity<User> link(String accessId, User user) {
        //Check User by GoogleId
        User authUser = userRepository.findFirstByGoogleId(accessId)
                .orElseGet(() -> user);

        if (!authUser.getId().equals(user.getId())) {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "Access Token and User Id doesn't match!");
        }

        if (Globalizer.isNullOrEmpty(user.getGoogleId())) {
            user.setGoogleId(accessId);
        } else {
            user.setGoogleId(null);
        }

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

}
