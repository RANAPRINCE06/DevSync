package com.devsync.notification.dto;

import com.devsync.notification.entity.Notification;
import com.devsync.notification.entity.NotificationChannel;
import com.devsync.notification.entity.NotificationStatus;
import com.devsync.notification.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String title;
    private String message;
    private UUID referenceId;
    private String referenceType;
    private Instant scheduledAt;
    private Instant sentAt;
    private Instant readAt;
    private Instant createdAt;
    private Instant updatedAt;

    public NotificationResponse() {
    }

    public NotificationResponse(UUID id, UUID userId, String userName, String userEmail, NotificationType type, NotificationChannel channel, NotificationStatus status, String title, String message, UUID referenceId, String referenceType, Instant scheduledAt, Instant sentAt, Instant readAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.type = type;
        this.channel = channel;
        this.status = status;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .userName(notification.getUser().getName())
                .userEmail(notification.getUser().getEmail())
                .type(notification.getType())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .scheduledAt(notification.getScheduledAt())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static NotificationResponseBuilder builder() {
        return new NotificationResponseBuilder();
    }

    public static class NotificationResponseBuilder {
        private UUID id;
        private UUID userId;
        private String userName;
        private String userEmail;
        private NotificationType type;
        private NotificationChannel channel;
        private NotificationStatus status;
        private String title;
        private String message;
        private UUID referenceId;
        private String referenceType;
        private Instant scheduledAt;
        private Instant sentAt;
        private Instant readAt;
        private Instant createdAt;
        private Instant updatedAt;

        public NotificationResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public NotificationResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public NotificationResponseBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public NotificationResponseBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public NotificationResponseBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public NotificationResponseBuilder channel(NotificationChannel channel) {
            this.channel = channel;
            return this;
        }

        public NotificationResponseBuilder status(NotificationStatus status) {
            this.status = status;
            return this;
        }

        public NotificationResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public NotificationResponseBuilder referenceId(UUID referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public NotificationResponseBuilder referenceType(String referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        public NotificationResponseBuilder scheduledAt(Instant scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public NotificationResponseBuilder sentAt(Instant sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public NotificationResponseBuilder readAt(Instant readAt) {
            this.readAt = readAt;
            return this;
        }

        public NotificationResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NotificationResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public NotificationResponse build() {
            return new NotificationResponse(id, userId, userName, userEmail, type, channel, status, title, message, referenceId, referenceType, scheduledAt, sentAt, readAt, createdAt, updatedAt);
        }
    }
}
