package com.sdm.auth.controller;

import com.google.zxing.WriterException;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.MultiFactorAuth;
import com.sdm.auth.service.MultiFactorAuthService;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.BarCodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/mfa")
public class MfaController extends DefaultController {

    @Autowired
    private MultiFactorAuthService multiFactorAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarCodeManager barCodeManager;

    @SuppressWarnings("Multi request need to control")
    @GetMapping("/resend")
    public ResponseEntity<MessageResponse> resendMfa(@RequestParam("userId") int userId,
                                                     @DefaultValue("") @RequestParam(value = "key", required = false) String mfaKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));
        if (user.isMfaEnabled()) {
            multiFactorAuthService.sendMfaCode(getCurrentUser().getUserId(), mfaKey);
        }
        return ResponseEntity.ok(new MessageResponse("Success!", "Sent new OTP code."));
    }

    @PostMapping("/")
    @Transactional
    public ResponseEntity<User> setupMfa(@Valid @RequestBody MultiFactorAuth mfa) {
        mfa.setUserId(getCurrentUser().getUserId());
        User user = multiFactorAuthService.setup(mfa);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/verify/{totp}")
    public ResponseEntity<MessageResponse> verifyMfa(@PathVariable(value = "totp") String totp,
                                                     @RequestParam(value = "key") String key) {
        if (multiFactorAuthService.verify(getCurrentUser().getUserId(), totp, key)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! Invalid otp code.");
        }
        return ResponseEntity.ok(new MessageResponse("Success!", "Thank you for your verfication."));
    }

    @GetMapping("/qr")
    public ResponseEntity<?> generateMfaQR(
            @RequestParam(value = "key") String key,
            @RequestParam(value = "width", required = false, defaultValue = "128") int width,
            @RequestParam(value = "noMargin", required = false, defaultValue = "false") boolean noMargin) throws IOException, WriterException {
        String mfaData = multiFactorAuthService.generateQrData(getCurrentUser().getUserId(), key);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        barCodeManager.createQR(outputStream, mfaData, width, noMargin);

        String filename = "user_mfa_qr.png";
        String attachment = "attachment; filename=\"" + filename + "\"";
        Resource resource = new ByteArrayResource(outputStream.toByteArray());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                .body(resource);
    }
}
