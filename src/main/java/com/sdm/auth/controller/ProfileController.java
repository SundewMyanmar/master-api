package com.sdm.auth.controller;

import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.AuthRequest;
import com.sdm.auth.model.request.ChangePasswordRequest;
import com.sdm.auth.model.request.TokenInfo;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.auth.service.FacebookAuthService;
import com.sdm.auth.service.GoogleAuthService;
import com.sdm.auth.service.JwtService;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.annotation.FileClassification;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.storage.model.File;
import com.sdm.storage.service.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequestMapping("/me")
public class ProfileController extends DefaultController {

	public enum LINK_TYPE {
		GOOGLE,
		FACEBOOK
	}

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TokenRepository tokenRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private GoogleAuthService googleAuthService;

	@Autowired
	private FacebookAuthService facebookAuthService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private FileService fileService;

	private User checkMe() {
		return userRepository.findById(getCurrentUser().getUserId())
				.orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST,
						localeManager.getMessage("invalid-user-account")));
	}

	@GetMapping("")
	public ResponseEntity<User> getProfile() {
		User user = this.checkMe();
		return ResponseEntity.ok(user);
	}

	@PostMapping("")
	public ResponseEntity<User> updateProfile(@RequestBody User user) {
		User existUser = this.checkMe();

		existUser.setProfileImage(user.getProfileImage());
		existUser.setDisplayName(user.getDisplayName());
		existUser.setNote(user.getNote());
		existUser.setExtras(user.getExtras());
		existUser = userRepository.save(existUser);

		return ResponseEntity.ok(existUser);
	}

	@PostMapping(value = "/changeProfileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<User> uploadFile(@RequestParam("profileImage") List<MultipartFile> files) {
		User existUser = this.checkMe();

		Field profileImageField = null;
		FileClassification annotation = null;
		try {
			profileImageField = User.class.getDeclaredField("profileImage");
			annotation = profileImageField.getAnnotation(FileClassification.class);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		if (files.size() > 0) {
			File file = fileService.create(files.get(0), null, annotation);
			existUser.setProfileImage(file);
			userRepository.save(existUser);
		}
		return new ResponseEntity<User>(existUser, HttpStatus.OK);
	}

	@PostMapping("/changePassword")
	public ResponseEntity<User> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		String phoneOrEmail = request.getUser();
		if (Globalizer.isPhoneNo(phoneOrEmail))
			phoneOrEmail = Globalizer.cleanPhoneNo(phoneOrEmail);

		User user = userRepository.findFirstByPhoneNumberOrEmail(phoneOrEmail, phoneOrEmail).orElseThrow(
				() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-user-account")));

		if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
			throw new GeneralException(HttpStatus.BAD_REQUEST,
					localeManager.getMessage("invalid-user-or-old-password"));
		}

		String newPassword = passwordEncoder.encode(request.getNewPassword());
		user.setPassword(newPassword);
		userRepository.save(user);

		return ResponseEntity.ok(user);
	}

	@PostMapping("/refreshToken")
	public ResponseEntity<User> refreshToken(@Valid @RequestBody TokenInfo tokenInfo,
			HttpServletRequest servletRequest) {
		User existUser = this.checkMe();

		String currentToken = jwtService.createToken(existUser, tokenInfo, servletRequest);
		existUser.setCurrentToken(currentToken);

		return ResponseEntity.ok(existUser);
	}

	@DeleteMapping("/cleanToken")
	public ResponseEntity<MessageResponse> cleanToken() {
		AuthRequest request = new AuthRequest();
		request.setDeviceId(getCurrentUser().getDeviceId());
		request.setDeviceOS(getCurrentUser().getDeviceOs());
		tokenRepository.cleanTokenByUserId(this.getCurrentUser().getUserId());
		MessageResponse message = new MessageResponse(HttpStatus.OK, localeManager.getMessage("remove-success"),
				localeManager.getMessage("clear-all-auth-token", this.getCurrentUser().getUserId()), null);
		return ResponseEntity.ok(message);
	}

	@GetMapping("/linkOAuth2/{type}")
	public ResponseEntity<User> linkOAuth2(@PathVariable(value = "type") LINK_TYPE type,
			@RequestParam(value = "accessId", defaultValue = "") String accessId) {
		User user = this.checkMe();

		if (type.equals(LINK_TYPE.GOOGLE)) {
			return googleAuthService.link(accessId, user);
		} else if (type.equals(LINK_TYPE.FACEBOOK)) {
			return facebookAuthService.link(accessId, user);
		}

		throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-auth-linked-type"));
	}
}
