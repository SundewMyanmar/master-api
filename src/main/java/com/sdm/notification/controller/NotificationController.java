package com.sdm.notification.controller;

import com.sdm.core.controller.DefaultReadController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.ListResponse;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.notification.model.Notification;
import com.sdm.notification.model.response.NotificationCount;
import com.sdm.notification.repository.NotificationRepository;
import com.sdm.notification.service.CloudMessagingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;

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
        if (notification.getUserId() > 0 && notification.getUserId() != getCurrentUser().getUserId()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("access-denied"));
        }
        return notification;
    }

    @GetMapping("/me")
    public ResponseEntity<PaginationResponse<Notification>> getMyNotificationsByType(@RequestParam(value = "page", defaultValue = "0") int pageId,
                                                                                     @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                                                     @RequestParam(value = "category", defaultValue = "") String category,
                                                                                     @RequestParam(value = "isTopic", defaultValue = "false") boolean isTopic) {
        Page<Notification> notifications;

        Pageable paging = this.buildPagination(pageId, pageSize, "sentAt:DESC");
        if (Globalizer.isNullOrEmpty(category)) {
            notifications = repository.pagingByUser(paging, getCurrentUser().getUserId());
        } else {
            if (isTopic) {
                notifications = repository.pagingByTopicAndCategory(paging, category);
            } else {
                notifications = repository.pagingByUserAndCategory(paging, getCurrentUser().getUserId(), category);
            }
        }
        return new ResponseEntity<>(new PaginationResponse<>(notifications), HttpStatus.PARTIAL_CONTENT);
    }

    @GetMapping("/me/count")
    public ResponseEntity<ListResponse<NotificationCount>> getStatus() {
        List<NotificationCount> unreadCount = repository.unreadCountWithCategory(getCurrentUser().getUserId());
        return ResponseEntity.ok(new ListResponse<>(unreadCount));
    }

    @Transactional
    @PutMapping("/me/readAll")
    public ResponseEntity<MessageResponse> readAllNotifications(@RequestParam(value = "category", defaultValue = "") String category) {
        if (Globalizer.isNullOrEmpty(category)) {
            repository.readAllNotifications(getCurrentUser().getUserId());
        } else {
            repository.readAllNotificationsByCategory(getCurrentUser().getUserId(), category);
        }

        MessageResponse message = new MessageResponse(localeManager.getMessage("success"), localeManager.getMessage("changed-notification-read-status"));
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
        MessageResponse message = new MessageResponse(localeManager.getMessage("remove-success"), localeManager.getMessage("remove-data"));
        return ResponseEntity.ok(message);
    }
}
