package com.devsync.notification.service;

import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.notification.dto.CreateNotificationRequest;
import com.devsync.notification.dto.NotificationResponse;
import com.devsync.notification.entity.Notification;
import com.devsync.notification.entity.NotificationChannel;
import com.devsync.notification.entity.NotificationStatus;
import com.devsync.notification.entity.NotificationType;
import com.devsync.notification.repository.NotificationRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User sampleUser;
    private Notification sampleNotification;
    private UUID userId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        sampleUser = User.builder()
                .id(userId)
                .name("Prince")
                .email("prince@devsync.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        sampleNotification = Notification.builder()
                .id(notificationId)
                .user(sampleUser)
                .type(NotificationType.DAILY_REMINDER)
                .channel(NotificationChannel.IN_APP)
                .status(NotificationStatus.PENDING)
                .title("Daily Progress Reminder")
                .message("Please submit your daily progress")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createNotification - success with default PENDING status")
    void createNotification_Success() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.DAILY_REMINDER)
                .title("Daily Progress Reminder")
                .message("Please submit your daily progress")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response);
        assertEquals(notificationId, response.getId());
        assertEquals("Daily Progress Reminder", response.getTitle());
        assertEquals(NotificationStatus.PENDING, response.getStatus());
        assertEquals(NotificationChannel.IN_APP, response.getChannel());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("createNotification - user not found throws ResourceNotFoundException")
    void createNotification_UserNotFound_ThrowsException() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.DAILY_REMINDER)
                .title("Title")
                .message("Message")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.createNotification(request));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("getNotificationById - success")
    void getNotificationById_Success() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(sampleNotification));

        NotificationResponse response = notificationService.getNotificationById(notificationId);

        assertNotNull(response);
        assertEquals(notificationId, response.getId());
        assertEquals("Prince", response.getUserName());
    }

    @Test
    @DisplayName("getNotificationById - not found or deleted throws ResourceNotFoundException")
    void getNotificationById_NotFound_ThrowsException() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.getNotificationById(notificationId));
    }

    @Test
    @DisplayName("markAsRead - transitions status to READ and sets readAt")
    void markAsRead_TransitionsStatusToRead() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse response = notificationService.markAsRead(notificationId);

        assertNotNull(response);
        assertEquals(NotificationStatus.READ, sampleNotification.getStatus());
        assertNotNull(sampleNotification.getReadAt());
        verify(notificationRepository).save(sampleNotification);
    }

    @Test
    @DisplayName("markAsRead - idempotent if already READ")
    void markAsRead_IdempotentIfAlreadyRead() {
        Instant previousReadAt = Instant.now().minusSeconds(60);
        sampleNotification.setStatus(NotificationStatus.READ);
        sampleNotification.setReadAt(previousReadAt);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(sampleNotification));

        NotificationResponse response = notificationService.markAsRead(notificationId);

        assertNotNull(response);
        assertEquals(previousReadAt, sampleNotification.getReadAt());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("markAllAsRead - updates all unread notifications")
    void markAllAsRead_UpdatesAllUnread() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(notificationRepository.findByUserIdAndStatusInAndDeletedFalse(eq(userId), anyCollection()))
                .thenReturn(List.of(sampleNotification));

        int count = notificationService.markAllAsRead(userId);

        assertEquals(1, count);
        assertEquals(NotificationStatus.READ, sampleNotification.getStatus());
        assertNotNull(sampleNotification.getReadAt());
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("getUnreadCount - returns count of unread notifications")
    void getUnreadCount_ReturnsCount() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(notificationRepository.countByUserIdAndStatusInAndDeletedFalse(eq(userId), anyCollection()))
                .thenReturn(3L);

        long count = notificationService.getUnreadCount(userId);

        assertEquals(3L, count);
    }

    @Test
    @DisplayName("deleteNotification - soft deletes notification by setting deleted=true")
    void deleteNotification_SoftDeletes() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        notificationService.deleteNotification(notificationId);

        assertTrue(sampleNotification.isDeleted());
        verify(notificationRepository).save(sampleNotification);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getUserNotifications - paginated list excludes deleted")
    void getUserNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> page = new PageImpl<>(List.of(sampleNotification));

        when(notificationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<NotificationResponse> result = notificationService.getUserNotifications(userId, NotificationType.DAILY_REMINDER, NotificationStatus.PENDING, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Daily Progress Reminder", result.getContent().get(0).getTitle());
    }
}
