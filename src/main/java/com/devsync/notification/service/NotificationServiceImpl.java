package com.devsync.notification.service;

import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.notification.dto.CreateNotificationRequest;
import com.devsync.notification.dto.NotificationResponse;
import com.devsync.notification.entity.Notification;
import com.devsync.notification.entity.NotificationStatus;
import com.devsync.notification.entity.NotificationType;
import com.devsync.notification.repository.NotificationRepository;
import com.devsync.notification.repository.NotificationSpecification;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .type(request.getType())
                .status(NotificationStatus.UNREAD)
                .title(request.getTitle().trim())
                .message(request.getMessage().trim())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType() != null ? request.getReferenceType().trim() : null)
                .build();

        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID userId, NotificationType type, NotificationStatus status, Pageable pageable) {
        Specification<Notification> spec = NotificationSpecification.filter(userId, type, status);
        return notificationRepository.findAll(spec, pageable).map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable) {
        if (userId != null && !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (userId != null) {
            return notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.UNREAD, pageable)
                    .map(NotificationResponse::fromEntity);
        }
        return notificationRepository.findAll(NotificationSpecification.withStatus(NotificationStatus.UNREAD), pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (notification.getStatus() != NotificationStatus.READ) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        Instant now = Instant.now();
        for (Notification notification : unreadNotifications) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(now);
        }

        notificationRepository.saveAll(unreadNotifications);
        return unreadNotifications.size();
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notificationRepository.delete(notification);
    }
}
