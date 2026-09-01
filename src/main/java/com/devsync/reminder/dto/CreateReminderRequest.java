package com.devsync.reminder.dto;

import com.devsync.reminder.entity.ReminderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class CreateReminderRequest {

    @NotNull(message = "User ID is required")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotNull(message = "Team ID is required")
    @Schema(example = "987e6543-e21b-12d3-a456-426614174000")
    private UUID teamId;

    @NotNull(message = "Reminder type is required")
    @Schema(example = "DAILY_PROGRESS")
    private ReminderType type;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "Daily Standup Reminder")
    private String title;

    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    @Schema(example = "Submit your daily progress and goal update before 9 PM")
    private String message;

    @NotNull(message = "Reminder time is required")
    @Schema(example = "18:00:00")
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

    public CreateReminderRequest() {
    }

    public CreateReminderRequest(UUID userId, UUID teamId, ReminderType type, String title, String message, LocalTime reminderTime, String timezone, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.teamId = teamId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.reminderTime = reminderTime;
        this.timezone = timezone;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public ReminderType getType() {
        return type;
    }

    public void setType(ReminderType type) {
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

    public static CreateReminderRequestBuilder builder() {
        return new CreateReminderRequestBuilder();
    }

    public static class CreateReminderRequestBuilder {
        private UUID userId;
        private UUID teamId;
        private ReminderType type;
        private String title;
        private String message;
        private LocalTime reminderTime;
        private String timezone;
        private LocalDate startDate;
        private LocalDate endDate;

        public CreateReminderRequestBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public CreateReminderRequestBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public CreateReminderRequestBuilder type(ReminderType type) {
            this.type = type;
            return this;
        }

        public CreateReminderRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CreateReminderRequestBuilder message(String message) {
            this.message = message;
            return this;
        }

        public CreateReminderRequestBuilder reminderTime(LocalTime reminderTime) {
            this.reminderTime = reminderTime;
            return this;
        }

        public CreateReminderRequestBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public CreateReminderRequestBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public CreateReminderRequestBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public CreateReminderRequest build() {
            return new CreateReminderRequest(userId, teamId, type, title, message, reminderTime, timezone, startDate, endDate);
        }
    }
}
