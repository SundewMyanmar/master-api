package com.sdm.auth.service;

import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.MultiFactorAuth;
import com.sdm.auth.model.MultiFactorAuthRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.util.Globalizer;
import com.sdm.sms.model.request.telenor.MessageType;
import com.sdm.sms.service.TelenorSmsService;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.CodeGenerationException;
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

    private User checkUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your account."));
    }

    private MultiFactorAuth getMainMfa(int userId) {
        return repository.findOneByUserIdAndMainTrue(userId)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your MFA."));
    }

    public String generateQrData(int userId, String key) {
        User user = this.checkUser(userId);
        MultiFactorAuth mfa = repository.findAppByUserId(userId)
                .orElseGet(() -> {
                    MultiFactorAuth newMfa = new MultiFactorAuth(userId);
                    if (!Globalizer.isNullOrEmpty(key)) {
                        newMfa.setKey(key);
                    }
                    repository.save(newMfa);
                    return newMfa;
                });
        String issuer = user.getDisplayName();
        if (Globalizer.isNullOrEmpty(issuer)) {
            issuer = "SUNDEW MYANMAR";
        }
        QrData data = qrDataFactory.newBuilder()
                .label(mfa.getKey())
                .secret(mfa.getSecret())
                .issuer(issuer)
                .digits(MFA_LENGTH)
                .period(mfa.getType().life())
                .build();

        return data.getUri();
    }

    @Transactional
    public void sendMfaCode(int userId, String key) {
        MultiFactorAuth mfa = null;
        if (!Globalizer.isNullOrEmpty(key)) {
            mfa = repository.findOneByUserIdAndKey(userId, key)
                    .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your mfa."));
        } else {
            mfa = getMainMfa(userId);
        }

        this.sendMfaCode(mfa);
    }

    @Transactional
    public void sendMfaCode(int userId) {
        MultiFactorAuth mfa = getMainMfa(userId);
        this.sendMfaCode(mfa);
    }

    @Transactional
    public void sendMfaCode(MultiFactorAuth mfa) {
        //Nothing to do if APP
        if (mfa.getType().equals(MultiFactorAuth.Type.APP)) {
            return;
        }

        if (Globalizer.isNullOrEmpty(mfa.getKey())) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Sorry! can't send verification code.");
        }

        String MFA_CHARS = "0123456789";
        String code = Globalizer.generateToken(MFA_CHARS, MFA_LENGTH);
        if (mfa.isTotp()) {
            try {
                code = codeGenerator.generate(mfa.getSecret(), mfa.getType().life());
            } catch (CodeGenerationException ex) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
            }
        } else {
            mfa.setSecret(code);
        }

        Date expiredDate = Globalizer.addDate(new Date(), Duration.ofSeconds(mfa.getType().life()));
        mfa.setSecretExpire(expiredDate);
        repository.save(mfa);

        if (mfa.getType().equals(MultiFactorAuth.Type.SMS)) {
            try {
                telenorSmsService.sendMessage(
                        "Your verification code is " + code + ".",
                        new String[]{mfa.getKey()}, MessageType.MULTILINGUAL);
            } catch (IOException ex) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
            }
        } else if (mfa.getType().equals(MultiFactorAuth.Type.EMAIL)) {
            mailService.sendMfa(mfa);
        } else {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Invalid MFA Type!");
        }
    }

    public boolean verify(int userId, String code, String key) {
        MultiFactorAuth mfa = getMainMfa(userId);

        if (!Globalizer.isNullOrEmpty(key)) {
            mfa = repository.findOneByUserIdAndKey(userId, key)
                    .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find your mfa account."));
        }
        boolean isValid = false;
        if (mfa.isTotp()) {
            isValid = codeVerifier.isValidCode(mfa.getSecret(), code);
        } else if (mfa.getSecretExpire().before(new Date())) {
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! your otp code has been expired.");
        }
        isValid = mfa.getSecret().equals(code);
        if (isValid && !mfa.isVerify()) {
            mfa.setVerify(true);
            repository.save(mfa);
        }

        return isValid;
    }

    @Transactional
    public User setup(MultiFactorAuth requestMfa) {
        User user = checkUser(requestMfa.getUserId());
        MultiFactorAuth mfa = new MultiFactorAuth(requestMfa);

        //APP type must be only one
        if (requestMfa.getType().equals(MultiFactorAuth.Type.APP)) {
            repository.findAppByUserId(requestMfa.getUserId())
                    .ifPresent((existMfa) -> {
                        mfa.setId(existMfa.getId());
                    });
        } else {
            repository.findOneByUserIdAndKey(requestMfa.getUserId(), requestMfa.getKey())
                    .ifPresent((existMfa) -> {
                        mfa.setId(existMfa.getId());
                    });
        }

        if (mfa.getType().equals(MultiFactorAuth.Type.APP)) {
            String secret = secretGenerator.generate();
            mfa.setTotp(true);
            mfa.setSecret(secret);
        } else if (Globalizer.isNullOrEmpty(requestMfa.getKey())) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! MFA Key is required.");
        }

        if (mfa.isMain()) {
            repository.clearMainMfa(mfa.getUserId());
        }

        if (!mfa.getType().equals(MultiFactorAuth.Type.APP)) {
            this.sendMfaCode(mfa);
        } else {
            //this.sendMfaCode has saved
            repository.save(mfa);
        }

        if (!user.isMfaEnabled()) {
            user.setMfaEnabled(true);
            userRepository.save(user);
        }

        return user;
    }

    @Transactional
    public User disable(int userId) {
        User user = this.checkUser(userId);
        repository.deleteByUserId(userId);
        user.setMfaEnabled(false);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public void remove(int userId, String id) {
        MultiFactorAuth mfa = repository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! can't find MFA."));
        if (mfa.isMain()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! You can't remove main MFA.");
        }
        if (mfa.getUserId() != userId) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! Can't find your MFA.");
        }
        repository.deleteById(id);
    }
}
