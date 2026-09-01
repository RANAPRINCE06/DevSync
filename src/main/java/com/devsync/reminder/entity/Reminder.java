package com.devsync.reminder.entity;

import com.devsync.team.entity.Team;
import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReminderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReminderStatus status = ReminderStatus.ACTIVE;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String message;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    @Column(nullable = false, length = 50)
    private String timezone;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Reminder() {
    }

    public Reminder(UUID id, User user, Team team, ReminderType type, ReminderStatus status, String title, String message, LocalTime reminderTime, String timezone, LocalDate startDate, LocalDate endDate, boolean active, Instant lastTriggeredAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.team = team;
        this.type = type;
        this.status = status != null ? status : ReminderStatus.ACTIVE;
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

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ReminderStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
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

    public static ReminderBuilder builder() {
        return new ReminderBuilder();
    }

    public static class ReminderBuilder {
        private UUID id;
        private User user;
        private Team team;
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

        public ReminderBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ReminderBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ReminderBuilder team(Team team) {
            this.team = team;
            return this;
        }

        public ReminderBuilder type(ReminderType type) {
            this.type = type;
            return this;
        }

        public ReminderBuilder status(ReminderStatus status) {
            this.status = status;
            return this;
        }

        public ReminderBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ReminderBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ReminderBuilder reminderTime(LocalTime reminderTime) {
            this.reminderTime = reminderTime;
            return this;
        }

        public ReminderBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public ReminderBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public ReminderBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public ReminderBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public ReminderBuilder lastTriggeredAt(Instant lastTriggeredAt) {
            this.lastTriggeredAt = lastTriggeredAt;
            return this;
        }

        public ReminderBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ReminderBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Reminder build() {
            return new Reminder(id, user, team, type, status, title, message, reminderTime, timezone, startDate, endDate, active, lastTriggeredAt, createdAt, updatedAt);
        }
    }
}
