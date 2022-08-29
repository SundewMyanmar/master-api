package com.sdm.admin.controller;

import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.ChangePasswordRequest;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.auth.service.AuthMailService;
import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.controller.WriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.storage.model.File;
import com.sdm.storage.service.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin/users")
public class UserController extends DefaultReadController<User, Integer> implements WriteController<User, Integer> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuthMailService mailService;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private FileService fileService;

    @Override
    protected DefaultRepository<User, Integer> getRepository() {
        return this.userRepository;
    }

    @PutMapping("/resetPassword/{userId}")
    public ResponseEntity<User> resetPassword(@PathVariable("userId") int userId,
                                              @Valid @RequestBody ChangePasswordRequest request) {
        User adminUser = this.checkData(getCurrentUser().getUserId());
        if (!passwordEncoder.matches(request.getOldPassword(), adminUser.getPassword())) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-admin-password"));
        }

        User existUser = this.checkData(userId);
        String newPassword = passwordEncoder.encode(request.getNewPassword());
        existUser.setPassword(newPassword);
        userRepository.save(existUser);

        return ResponseEntity.ok(existUser);
    }

    @DeleteMapping("/cleanToken/{userId}")
    public ResponseEntity<MessageResponse> cleanToken(@PathVariable("userId") int userId) {
        User existUser = this.checkData(userId);
        tokenRepository.cleanTokenByUserId(existUser.getId());
        MessageResponse message = new MessageResponse(localeManager.getMessage("success"),
                localeManager.getMessage("clear-all-auth-token", this.getCurrentUser().getUserId()));
        return ResponseEntity.ok(message);
    }

    @Override
    public ResponseEntity<User> create(@Valid @RequestBody User request) {
        // Check user by user name
        userRepository.findFirstByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail()).ifPresent(user -> {
            if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("already-registered-email"));
            } else if (user.getPhoneNumber().equalsIgnoreCase(request.getPhoneNumber())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("already-registered-phone"));
            }
        });

        String rawPassword = request.getPassword();
        String password = passwordEncoder.encode(rawPassword);
        request.setPassword(password);
        request.setPhoneNumber(Globalizer.cleanPhoneNo(request.getPhoneNumber()));

        User entity = userRepository.save(request);
        mailService.welcomeUser(entity, rawPassword, "Welcome!");

        return new ResponseEntity<>(entity, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<User> update(@Valid @RequestBody User body, @PathVariable("id") Integer id) {
        User dbEntity = this.getRepository().findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        localeManager.getMessage("no-data-by", id)));

        if (!id.equals(body.getId())) {
            throw new GeneralException(HttpStatus.CONFLICT, localeManager.getMessage("not-match-path-body-id"));
        }

        body.setPassword(dbEntity.getPassword());
        body.setVersion(dbEntity.getVersion());
        body.setPhoneNumber(Globalizer.cleanPhoneNo(body.getPhoneNumber()));

        User entity = getRepository().save(body);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<User> partialUpdate(Map<String, Object> body, Integer id) {
        User existUser = this.checkData(id);

        body.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(getEntityClass(), key);
            if (field != null && !field.isAnnotationPresent(Id.class) && !field.getName().equalsIgnoreCase("password")) {
                Object fieldValue = value;

                if (field.getName().equalsIgnoreCase("phoneNumber")) {
                    fieldValue = Globalizer.cleanPhoneNo(value.toString());
                }
                ReflectionUtils.setField(field, existUser, fieldValue);
            }
        });

        User entity = getRepository().save(existUser);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<MessageResponse> remove(Integer id) {
        User existEntity = this.checkData(id);
        getRepository().softDelete(existEntity);
        MessageResponse message = new MessageResponse(localeManager.getMessage("remove-success"),
                localeManager.getMessage("remove-data-by", id));
        return ResponseEntity.ok(message);
    }

    @Override
    public ResponseEntity<MessageResponse> multiRemove(@Valid List<Integer> ids) {
        ids.forEach(id -> getRepository().softDeleteById(id));
        MessageResponse message = new MessageResponse(localeManager.getMessage("remove-success"),
                localeManager.getMessage("remove-multi-data", ids.size()));
        return ResponseEntity.ok(message);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> uploadFile(MultipartFile file, String fieldName, Integer folder) {
        File fileEntity = fileService.create(file, folder, this.getFileClassification(this.getEntityClass(), fieldName));
        return new ResponseEntity<>(fileEntity, HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<ListResponse<User>> importData(@Valid List<User> body) {
        ListResponse<User> response = new ListResponse<User>();
        for (final User user : body) {
            String rawPassword = user.getPassword();
            if (!Globalizer.isNullOrEmpty(rawPassword)) {
                String password = passwordEncoder.encode(rawPassword);
                user.setPassword(password);
            }

            user.setPhoneNumber(Globalizer.cleanPhoneNo(user.getPhoneNumber()));
            getRepository().save(user);
            response.addData(user);
        }

        return new ResponseEntity<ListResponse<User>>(response, HttpStatus.OK);
    }


}
