package com.devsync.notification.service;

import com.devsync.notification.dto.CreateNotificationRequest;
import com.devsync.notification.dto.NotificationResponse;
import com.devsync.notification.entity.NotificationStatus;
import com.devsync.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotificationById(UUID id);

    Page<NotificationResponse> getUserNotifications(UUID userId, NotificationType type, NotificationStatus status, Pageable pageable);

    NotificationResponse markAsRead(UUID id);

    int markAllAsRead(UUID userId);

    long getUnreadCount(UUID userId);

    void deleteNotification(UUID id);
}
