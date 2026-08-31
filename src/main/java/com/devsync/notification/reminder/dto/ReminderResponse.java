package com.devsync.notification.reminder.dto;

import com.devsync.notification.reminder.entity.Reminder;
import com.devsync.notification.reminder.entity.ReminderType;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public class ReminderResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID teamId;
    private String teamName;
    private ReminderType type;
    private String title;
    private String message;
    private LocalTime reminderTime;
    private String timezone;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public ReminderResponse() {
    }

    public ReminderResponse(UUID id, UUID userId, String userName, String userEmail, UUID teamId, String teamName, ReminderType type, String title, String message, LocalTime reminderTime, String timezone, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.teamId = teamId;
        this.teamName = teamName;
        this.type = type;
        this.title = title;
        this.message = message;
        this.reminderTime = reminderTime;
        this.timezone = timezone;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReminderResponse fromEntity(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .userId(reminder.getUser().getId())
                .userName(reminder.getUser().getName())
                .userEmail(reminder.getUser().getEmail())
                .teamId(reminder.getTeam() != null ? reminder.getTeam().getId() : null)
                .teamName(reminder.getTeam() != null ? reminder.getTeam().getName() : null)
                .type(reminder.getType())
                .title(reminder.getTitle())
                .message(reminder.getMessage())
                .reminderTime(reminder.getReminderTime())
                .timezone(reminder.getTimezone())
                .active(reminder.isActive())
                .createdAt(reminder.getCreatedAt())
                .updatedAt(reminder.getUpdatedAt())
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

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public static ReminderResponseBuilder builder() {
        return new ReminderResponseBuilder();
    }

    public static class ReminderResponseBuilder {
        private UUID id;
        private UUID userId;
        private String userName;
        private String userEmail;
        private UUID teamId;
        private String teamName;
        private ReminderType type;
        private String title;
        private String message;
        private LocalTime reminderTime;
        private String timezone;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public ReminderResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ReminderResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public ReminderResponseBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public ReminderResponseBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public ReminderResponseBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public ReminderResponseBuilder teamName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public ReminderResponseBuilder type(ReminderType type) {
            this.type = type;
            return this;
        }

        public ReminderResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ReminderResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ReminderResponseBuilder reminderTime(LocalTime reminderTime) {
            this.reminderTime = reminderTime;
            return this;
        }

        public ReminderResponseBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public ReminderResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public ReminderResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ReminderResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ReminderResponse build() {
            return new ReminderResponse(id, userId, userName, userEmail, teamId, teamName, type, title, message, reminderTime, timezone, active, createdAt, updatedAt);
        }
    }
}
