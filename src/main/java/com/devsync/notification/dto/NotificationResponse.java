package com.devsync.notification.dto;

import com.devsync.notification.entity.Notification;
import com.devsync.notification.entity.NotificationStatus;
import com.devsync.notification.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private NotificationStatus status;
    private String title;
    private String message;
    private UUID referenceId;
    private String referenceType;
    private Instant readAt;
    private Instant createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(UUID id, UUID userId, NotificationType type, NotificationStatus status, String title, String message, UUID referenceId, String referenceType, Instant readAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.readAt = readAt;
        this.createdAt = createdAt;
    }

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .status(notification.getStatus())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
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

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
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

    public static NotificationResponseBuilder builder() {
        return new NotificationResponseBuilder();
    }

    public static class NotificationResponseBuilder {
        private UUID id;
        private UUID userId;
        private NotificationType type;
        private NotificationStatus status;
        private String title;
        private String message;
        private UUID referenceId;
        private String referenceType;
        private Instant readAt;
        private Instant createdAt;

        public NotificationResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public NotificationResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public NotificationResponseBuilder type(NotificationType type) {
            this.type = type;
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

        public NotificationResponseBuilder readAt(Instant readAt) {
            this.readAt = readAt;
            return this;
        }

        public NotificationResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NotificationResponse build() {
            return new NotificationResponse(id, userId, type, status, title, message, referenceId, referenceType, readAt, createdAt);
        }
    }
}
