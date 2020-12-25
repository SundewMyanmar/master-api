package com.sdm.auth.service;

import com.sdm.admin.model.User;
import com.sdm.auth.model.request.OAuth2Request;
import org.springframework.http.ResponseEntity;

public interface SocialAuthService {
    ResponseEntity<User> auth(OAuth2Request request);

    ResponseEntity<User> link(String accessId, User user);
}
