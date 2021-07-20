package com.sdm.auth.controller;

import com.google.zxing.WriterException;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.MultiFactorAuth;
import com.sdm.auth.repository.MultiFactorAuthRepository;
import com.sdm.auth.service.MultiFactorAuthService;
import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.core.util.BarCodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
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
    private MultiFactorAuthRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarCodeManager barCodeManager;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginationResponse<MultiFactorAuth>> getPaging(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                         @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                                         @RequestParam(value = "sort", defaultValue = "modifiedAt:DESC") String sort) {
        Page<MultiFactorAuth> paging = this.repository.findByUserIdAndVerifyTrue(getCurrentUser().getUserId(), this.buildPagination(page, pageSize, sort));
        PaginationResponse<MultiFactorAuth> response = new PaginationResponse<>(paging);

        return new ResponseEntity<>(response, HttpStatus.PARTIAL_CONTENT);
    }

    @GetMapping("/resend")
    public ResponseEntity<MessageResponse> resendMfa(@RequestParam("userId") int userId,
                                                     @DefaultValue("") @RequestParam(value = "key", required = false) String mfaKey) {
        multiFactorAuthService.sendMfaCode(getCurrentUser().getUserId(), mfaKey);
        return ResponseEntity.ok(new MessageResponse("Success!", "Sent new OTP code."));
    }

    @PostMapping("/setup")
    @Transactional
    public ResponseEntity<MessageResponse> setupMfa(@Valid @RequestBody MultiFactorAuth mfa) {
        mfa.setUserId(getCurrentUser().getUserId());
        multiFactorAuthService.setupEmailOrSMS(mfa);
        return ResponseEntity.ok(new MessageResponse("Success!", "Sent verification code to " + mfa.getKey() + "."));
    }

    @GetMapping("/disable")
    public ResponseEntity<MessageResponse> disableMfa() {
        multiFactorAuthService.disable(getCurrentUser().getUserId());
        return ResponseEntity.ok(new MessageResponse("Success!", "Disabled 2-Step verification."));
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<MessageResponse> removeMfa(@PathVariable(value = "id") String id) {
        multiFactorAuthService.remove(getCurrentUser().getUserId(), id);
        MessageResponse message = new MessageResponse("DELETED",
                "Deleted data on your request by.");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/default/{id}")
    public ResponseEntity<MultiFactorAuth> setDefaultById(@PathVariable(value = "id") String id) {
        MultiFactorAuth mfa = multiFactorAuthService.setDefaultMfa(getCurrentUser().getUserId(), id);
        return ResponseEntity.ok(mfa);
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyMfa(@RequestParam(value = "totp") String totp,
                                                     @DefaultValue("") @RequestParam(value = "key", required = false) String key) {
        if (!multiFactorAuthService.verify(getCurrentUser().getUserId(), totp, key)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! Invalid otp code.");
        }
        return ResponseEntity.ok(new MessageResponse("Success!", "Thank you for your verification."));
    }

    @GetMapping("/qr")
    public ResponseEntity<?> generateMfaQR(
            @RequestParam(value = "width", required = false, defaultValue = "128") int width,
            @RequestParam(value = "noMargin", required = false, defaultValue = "false") boolean noMargin) throws IOException, WriterException {
        String mfaData = multiFactorAuthService.generateQrData(getCurrentUser().getUserId());
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
