package com.sdm.admin.controller;

import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.ChangePasswordRequest;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.auth.service.AuthMailService;
import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.controller.ReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Id;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class UserController extends DefaultReadController<User, Integer> implements ReadWriteController<User, Integer> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuthMailService mailService;

    @Autowired
    private SecurityManager securityManager;

    @Override
    protected DefaultRepository<User, Integer> getRepository() {
        return this.userRepository;
    }

    @PutMapping("/resetPassword/{userId}")
    public ResponseEntity<User> resetPassword(@PathVariable("userId") int userId,
                                              @Valid @RequestBody ChangePasswordRequest request) {
        User adminUser = this.checkData(getCurrentUser().getUserId());
        String adminPassword = securityManager.hashString(request.getOldPassword());
        if (!adminUser.getPassword().equals(adminPassword)) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! administartor password is incorrect.");
        }

        User existUser = this.checkData(userId);
        String newPassword = securityManager.hashString(request.getNewPassword());
        existUser.setPassword(newPassword);
        userRepository.save(existUser);

        return ResponseEntity.ok(existUser);
    }

    @DeleteMapping("/cleanToken/{userId}")
    public ResponseEntity<MessageResponse> cleanToken(@PathVariable("userId") int userId) {
        User existUser = this.checkData(userId);
        tokenRepository.cleanTokenByUserId(existUser.getId());
        MessageResponse message = new MessageResponse("success",
                "Cleaned all token by User ID : " + this.getCurrentUser().getUserId());
        return ResponseEntity.ok(message);
    }

    @Override
    public ResponseEntity<User> create(@Valid @RequestBody User request) {
        // Check user by user name
        userRepository.findFirstByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail()).ifPresent(user -> {
            if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this email");
            } else if (user.getPhoneNumber().equalsIgnoreCase(request.getPhoneNumber())) {
                throw new GeneralException(HttpStatus.BAD_REQUEST,
                        "Sorry! someone already registered with this phone number.");
            }
        });

        String rawPassword = request.getPassword();
        String password = securityManager.hashString(rawPassword);
        request.setPassword(password);

        User entity = userRepository.save(request);
        mailService.welcomeUser(entity, rawPassword, "Welcome!");

        return new ResponseEntity<>(entity, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<User> update(@Valid @RequestBody User body, @PathVariable("id") Integer id) {
        User dbEntity = this.getRepository().findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "There is no any data by : " + id));

        if (!id.equals(body.getId())) {
            throw new GeneralException(HttpStatus.CONFLICT, "Path ID and body ID aren't match.");
        }

        body.setPassword(dbEntity.getPassword());
        body.setVersion(dbEntity.getVersion());

        User entity = getRepository().save(body);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<User> partialUpdate(Map<String, Object> body, Integer id) {
        User existUser = this.checkData(id);

        body.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(getEntityClass(), key);
            if (field != null && !field.isAnnotationPresent(Id.class) && !field.getName().equalsIgnoreCase("password")) {
                ReflectionUtils.setField(field, existUser, value);
            }
        });

        User entity = getRepository().save(existUser);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<MessageResponse> remove(Integer id) {
        User existEntity = this.checkData(id);
        getRepository().softDelete(existEntity);
        MessageResponse message = new MessageResponse(HttpStatus.OK, "successfully_deleted",
                "Deleted data on your request by : " + id.toString(), null);
        return ResponseEntity.ok(message);
    }

    @Override
    public ResponseEntity<MessageResponse> multiRemove(@Valid List<Integer> ids) {
        ids.forEach(id -> getRepository().softDeleteById(id));
        MessageResponse message = new MessageResponse(HttpStatus.OK, "DELETED",
                "Deleted " + ids.size() + " data.", null);
        return ResponseEntity.ok(message);
    }

    @Override
    @Transactional
    public ResponseEntity<ListResponse<User>> importData(@Valid List<User> body) {
        ListResponse response = new ListResponse();
        for (final User user : body) {
            String rawPassword = user.getPassword();
            if (!StringUtils.isEmpty(rawPassword)) {
                String password = securityManager.hashString(rawPassword);
                user.setPassword(password);
            }
            getRepository().save(user);
            response.addData(user);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
