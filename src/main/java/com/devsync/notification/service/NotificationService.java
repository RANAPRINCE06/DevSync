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

    Page<NotificationResponse> getNotifications(UUID userId, NotificationType type, NotificationStatus status, Pageable pageable);

    Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable);

    long getUnreadCount(UUID userId);

    NotificationResponse markAsRead(UUID id);

    int markAllAsRead(UUID userId);

    void deleteNotification(UUID id);
}
