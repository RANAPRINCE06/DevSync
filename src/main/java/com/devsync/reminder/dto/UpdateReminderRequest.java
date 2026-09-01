package com.devsync.reminder.dto;

import com.devsync.reminder.entity.ReminderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
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

    @NotNull(message = "Start date is required")
    @Schema(example = "2026-09-01")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(example = "2026-12-31")
    private LocalDate endDate;

    @NotNull(message = "Status is required")
    @Schema(example = "ACTIVE")
    private ReminderStatus status;

    public UpdateReminderRequest() {
    }

    public UpdateReminderRequest(String title, String message, LocalTime reminderTime, String timezone, LocalDate startDate, LocalDate endDate, ReminderStatus status) {
        this.title = title;
        this.message = message;
        this.reminderTime = reminderTime;
        this.timezone = timezone;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public static UpdateReminderRequestBuilder builder() {
        return new UpdateReminderRequestBuilder();
    }

    public static class UpdateReminderRequestBuilder {
        private String title;
        private String message;
        private LocalTime reminderTime;
        private String timezone;
        private LocalDate startDate;
        private LocalDate endDate;
        private ReminderStatus status = ReminderStatus.ACTIVE;

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

        public UpdateReminderRequestBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public UpdateReminderRequestBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public UpdateReminderRequestBuilder status(ReminderStatus status) {
            this.status = status;
            return this;
        }

        public UpdateReminderRequest build() {
            return new UpdateReminderRequest(title, message, reminderTime, timezone, startDate, endDate, status);
        }
    }
}
