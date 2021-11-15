package com.sdm.auth.service;

import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.MultiFactorAuth;
import com.sdm.auth.repository.MultiFactorAuthRepository;
import com.sdm.core.Constants;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.telenor.model.request.telenor.MessageType;
import com.sdm.telenor.service.TelenorSmsService;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Service
public class MultiFactorAuthService {

    private final int MFA_LENGTH = 6;
    @Autowired
    private MultiFactorAuthRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecretGenerator secretGenerator;
    @Autowired
    private CodeGenerator codeGenerator;
    @Autowired
    private CodeVerifier codeVerifier;
    @Autowired
    private QrDataFactory qrDataFactory;
    @Autowired
    private RecoveryCodeGenerator recoveryCodeGenerator;
    @Autowired
    private TelenorSmsService telenorSmsService;
    @Autowired
    private AuthMailService mailService;
    @Autowired
    private LocaleManager localeManager;

    private User checkUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("invalid-user-account")));
    }

    @Transactional
    public MultiFactorAuth authMfa(int userId, String key, boolean useMain) {
        Optional<MultiFactorAuth> mfa;
        if (!Globalizer.isNullOrEmpty(key)) {
            mfa = repository.findOneByUserIdAndMfaKeyAndVerifyTrue(userId, key);
        } else {
            mfa = repository.findAppByUserIdAndVerifyTrue(userId);
        }

        if (useMain) {
            mfa = repository.findOneByUserIdAndMainTrueAndVerifyTrue(userId);
        }

        if (!mfa.isPresent()) {
            return null;
        }

        return mfa.get();
    }

    public String generateQrData(int userId) {
        User user = this.checkUser(userId);
        MultiFactorAuth mfa = repository.findAppByUserId(userId)
                .orElseGet(() -> {
                    MultiFactorAuth newMfa = new MultiFactorAuth(userId);
                    newMfa.setSecret(secretGenerator.generate());
                    repository.save(newMfa);
                    return newMfa;
                });
        String label = user.getDisplayName();
        if (Globalizer.isNullOrEmpty(label)) {
            label = "SUNDEW MYANMAR";
        }
        QrData data = qrDataFactory.newBuilder()
                .label(label)
                .secret(mfa.getSecret())
                .issuer(Constants.APP_NAME)
                .digits(MFA_LENGTH)
                .period(mfa.getType().life())
                .build();

        return data.getUri();
    }

    @Transactional
    public void sendMfaCode(int userId, String key) {
        MultiFactorAuth mfa;
        if (!Globalizer.isNullOrEmpty(key)) {
            mfa = repository.findOneByUserIdAndMfaKey(userId, key)
                    .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("no-data")));
        } else {
            mfa = repository.findAppByUserId(userId)
                    .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("no-data")));
        }

        this.sendMfaCode(mfa);
    }

    @Transactional
    public void sendMfaCode(MultiFactorAuth mfa) {
        //Nothing to do if APP
        if (mfa.getType().equals(MultiFactorAuth.Type.APP)) {
            return;
        }

        if (Globalizer.isNullOrEmpty(mfa.getMfaKey())) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, localeManager.getMessage("required-mfa-key"));
        }

        String MFA_CHARS = "0123456789";
        String code = Globalizer.generateToken(MFA_CHARS, MFA_LENGTH);
        Date expiredDate = Globalizer.addDate(new Date(), Duration.ofSeconds(mfa.getType().life()));
        mfa.setSecret(code);
        mfa.setSecretExpire(expiredDate);
        repository.save(mfa);

        if (mfa.getType().equals(MultiFactorAuth.Type.SMS)) {
            try {
                telenorSmsService.sendMessage(
                        "Your verification code is " + code + ".",
                        new String[]{mfa.getMfaKey()}, MessageType.MULTILINGUAL);
            } catch (IOException ex) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
            }
        } else if (mfa.getType().equals(MultiFactorAuth.Type.EMAIL)) {
            mailService.sendMfa(mfa.getMfaKey(), code, expiredDate);
        } else {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-mfa-type"));
        }
    }

    @Transactional
    public boolean verify(int userId, String code, String key) {
        MultiFactorAuth mfa;
        if (!Globalizer.isNullOrEmpty(key)) {
            mfa = repository.findOneByUserIdAndMfaKey(userId, key)
                    .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("no-data")));
        } else {
            mfa = repository.findAppByUserId(userId)
                    .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("no-data")));
        }

        boolean isValid = false;
        if (mfa.getType().equals(MultiFactorAuth.Type.APP)) {
            isValid = codeVerifier.isValidCode(mfa.getSecret(), code);
        } else {
            if (mfa.getSecretExpire().before(new Date())) {
                throw new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("otp-expired"));
            }

            isValid = mfa.getSecret().equals(code);
        }

        if (isValid && !mfa.isVerify()) {
            repository.clearMainMfa(mfa.getUserId());
            mfa.setVerify(true);
            mfa.setMain(true);
            repository.save(mfa);
        }

        return isValid;
    }

    @Transactional
    public void setupEmailOrSMS(MultiFactorAuth requestMfa) {
        checkUser(requestMfa.getUserId());
        MultiFactorAuth mfa = new MultiFactorAuth(requestMfa);

        if (mfa.getType().equals(MultiFactorAuth.Type.APP)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("verify-mfa-key", "Authenticator App"));
        }

        repository.findOneByUserIdAndMfaKey(mfa.getUserId(), mfa.getMfaKey())
                .ifPresent((existMfa) -> {
                    mfa.setId(existMfa.getId());
                    mfa.setVersion(existMfa.getVersion());
                });

        if (Globalizer.isNullOrEmpty(mfa.getMfaKey())) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("required-mfa-key"));
        }

        this.sendMfaCode(mfa);
    }

    @Transactional
    public MultiFactorAuth setDefaultMfa(int userId, String id) {
        MultiFactorAuth mfa = repository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("no-data")));
        if (mfa.isMain()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("default-mfa-remove"));
        }
        if (mfa.getUserId() != userId) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("no-data"));
        }
        repository.clearMainMfa(userId);
        mfa.setMain(true);
        repository.save(mfa);
        return mfa;
    }

    @Transactional
    public void disable(int userId) {
        repository.disableAllMfa(userId);
    }

    @Transactional
    public void remove(int userId, String id) {
        MultiFactorAuth mfa = repository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, localeManager.getMessage("no-data")));
        if (mfa.isMain()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("default-mfa-remove"));
        }
        if (mfa.getUserId() != userId) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("no-data"));
        }
        mfa.setVerify(false);
        repository.save(mfa);
    }
}
