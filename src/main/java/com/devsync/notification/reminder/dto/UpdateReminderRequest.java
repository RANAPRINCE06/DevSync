package com.devsync.notification.reminder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public class UpdateReminderRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "Updated Daily Standup Reminder")
    private String title;

    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    @Schema(example = "Updated reminder description")
    private String message;

    @NotNull(message = "Reminder time is required")
    @Schema(example = "19:00:00")
    private LocalTime reminderTime;

    @NotBlank(message = "Timezone is required")
    @Schema(example = "Asia/Kolkata")
    private String timezone;

    @NotNull(message = "Active state is required")
    @Schema(example = "true")
    private Boolean active;

    public UpdateReminderRequest() {
    }

    public UpdateReminderRequest(String title, String message, LocalTime reminderTime, String timezone, Boolean active) {
        this.title = title;
        this.message = message;
        this.reminderTime = reminderTime;
        this.timezone = timezone;
        this.active = active;
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

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public static UpdateReminderRequestBuilder builder() {
        return new UpdateReminderRequestBuilder();
    }

    public static class UpdateReminderRequestBuilder {
        private String title;
        private String message;
        private LocalTime reminderTime;
        private String timezone;
        private Boolean active = true;

        public UpdateReminderRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public UpdateReminderRequestBuilder message(String message) {
            this.message = message;
            return this;
        }

        public UpdateReminderRequestBuilder reminderTime(LocalTime reminderTime) {
            this.reminderTime = reminderTime;
            return this;
        }

        public UpdateReminderRequestBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public UpdateReminderRequestBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public UpdateReminderRequest build() {
            return new UpdateReminderRequest(title, message, reminderTime, timezone, active);
        }
    }
}
