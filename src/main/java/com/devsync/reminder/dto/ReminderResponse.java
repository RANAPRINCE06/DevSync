package com.devsync.reminder.dto;

import com.devsync.reminder.entity.Reminder;
import com.devsync.reminder.entity.ReminderStatus;
import com.devsync.reminder.entity.ReminderType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class ReminderResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private UUID teamId;
    private String teamName;
    private ReminderType type;
    private ReminderStatus status;
    private String title;
    private String message;
    private LocalTime reminderTime;
    private String timezone;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private Instant lastTriggeredAt;
    private Instant createdAt;
    private Instant updatedAt;

    public ReminderResponse() {
    }

    public ReminderResponse(UUID id, UUID userId, String userName, UUID teamId, String teamName, ReminderType type, ReminderStatus status, String title, String message, LocalTime reminderTime, String timezone, LocalDate startDate, LocalDate endDate, boolean active, Instant lastTriggeredAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.teamId = teamId;
        this.teamName = teamName;
        this.type = type;
        this.status = status;
        this.title = title;
        this.message = message;
        this.reminderTime = reminderTime;
        this.timezone = timezone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.lastTriggeredAt = lastTriggeredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReminderResponse fromEntity(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .userId(reminder.getUser().getId())
                .userName(reminder.getUser().getName())
                .teamId(reminder.getTeam().getId())
                .teamName(reminder.getTeam().getName())
                .type(reminder.getType())
                .status(reminder.getStatus())
                .title(reminder.getTitle())
                .message(reminder.getMessage())
                .reminderTime(reminder.getReminderTime())
                .timezone(reminder.getTimezone())
                .startDate(reminder.getStartDate())
                .endDate(reminder.getEndDate())
                .active(reminder.isActive())
                .lastTriggeredAt(reminder.getLastTriggeredAt())
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

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderStatus status) {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(Instant lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
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
        private UUID teamId;
        private String teamName;
        private ReminderType type;
        private ReminderStatus status = ReminderStatus.ACTIVE;
        private String title;
        private String message;
        private LocalTime reminderTime;
        private String timezone;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean active = true;
        private Instant lastTriggeredAt;
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

        public ReminderResponseBuilder status(ReminderStatus status) {
            this.status = status;
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

        public ReminderResponseBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public ReminderResponseBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public ReminderResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public ReminderResponseBuilder lastTriggeredAt(Instant lastTriggeredAt) {
            this.lastTriggeredAt = lastTriggeredAt;
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
            return new ReminderResponse(id, userId, userName, teamId, teamName, type, status, title, message, reminderTime, timezone, startDate, endDate, active, lastTriggeredAt, createdAt, updatedAt);
        }
    }
}
