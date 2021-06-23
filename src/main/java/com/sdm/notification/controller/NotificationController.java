package com.sdm.notification.controller;

import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.notification.model.Notification;
import com.sdm.notification.repository.NotificationRepository;
import com.sdm.notification.service.CloudMessagingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Date;

@Log4j2
@RestController
@RequestMapping("/notifications")
public class NotificationController extends DefaultReadController<Notification, String> {
    @Autowired
    private NotificationRepository repository;

    @Autowired
    private CloudMessagingService cloudMessagingService;

    @Override
    protected DefaultRepository<Notification, String> getRepository() {
        return repository;
    }

    private Notification checkUserNoti(String id) {
        Notification notification = this.checkData(id);
        if (notification.getUser() != null && !notification.getUser().getId().equals(getCurrentUser().getUserId())) {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "You don't have access the message.");
        }
        return notification;
    }

    @GetMapping("/me")
    public ResponseEntity<PaginationResponse<Notification>> getMyNotificationsByType(@RequestParam(value = "page", defaultValue = "0") int pageId,
                                                                                     @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                                                     @RequestParam(value = "category", defaultValue = "") String category) {
        Page<Notification> notifications;

        Pageable paging = this.buildPagination(pageId, pageSize, "sentAt:DESC");
        if (StringUtils.isEmpty(category)) {
            notifications = repository.pagingByUser(paging, getCurrentUser().getUserId());
        } else {
            notifications = repository.pagingByUserAndCategory(paging, getCurrentUser().getUserId(), category);
        }
        return new ResponseEntity<>(new PaginationResponse<>(notifications), HttpStatus.PARTIAL_CONTENT);
    }

    @Transactional
    @PutMapping("/me/readAll")
    public ResponseEntity<MessageResponse> readAllNotifications(@RequestParam(value = "category", defaultValue = "") String category) {
        if (StringUtils.isEmpty(category)) {
            repository.readAllNotifications(getCurrentUser().getUserId());
        } else {
            repository.readAllNotificationsByCategory(getCurrentUser().getUserId(), category);
        }

        MessageResponse message = new MessageResponse(HttpStatus.OK, "Changed read status on all notification.");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<Notification> readNotifications(@PathVariable("id") String id) {
        Notification notification = this.checkUserNoti(id);
        notification.setReadAt(new Date());
        Notification entity = getRepository().save(notification);
        return ResponseEntity.ok(entity);
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable("id") String id) {
        Notification notification = this.checkUserNoti(id);
        repository.softDelete(notification);
        MessageResponse message = new MessageResponse(HttpStatus.OK, "Deleted notification on your request.");
        return ResponseEntity.ok(message);
    }

    @PostMapping(value = "/fcm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> sendFcmMessage(@Valid @RequestBody Notification notification) {
        cloudMessagingService.sendMessage(notification);
        MessageResponse message = new MessageResponse(HttpStatus.OK, "Sent notification on your request.");
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
}
