package com.devsync.notification.dto;

import com.devsync.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateNotificationRequest {

    @NotNull(message = "User ID is required")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotNull(message = "Notification type is required")
    @Schema(example = "DAILY_REMINDER")
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "Daily Progress Submission Reminder")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    @Schema(example = "Don't forget to submit your daily learning progress before 9:00 PM!")
    private String message;

    @Schema(example = "987e6543-e21b-12d3-a456-426614174000")
    private UUID referenceId;

    @Schema(example = "GOAL")
    private String referenceType;

    public CreateNotificationRequest() {
    }

    public CreateNotificationRequest(UUID userId, NotificationType type, String title, String message, UUID referenceId, String referenceType) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
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

    public static CreateNotificationRequestBuilder builder() {
        return new CreateNotificationRequestBuilder();
    }

    public static class CreateNotificationRequestBuilder {
        private UUID userId;
        private NotificationType type;
        private String title;
        private String message;
        private UUID referenceId;
        private String referenceType;

        public CreateNotificationRequestBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public CreateNotificationRequestBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public CreateNotificationRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CreateNotificationRequestBuilder message(String message) {
            this.message = message;
            return this;
        }

        public CreateNotificationRequestBuilder referenceId(UUID referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public CreateNotificationRequestBuilder referenceType(String referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        public CreateNotificationRequest build() {
            return new CreateNotificationRequest(userId, type, title, message, referenceId, referenceType);
        }
    }
}
