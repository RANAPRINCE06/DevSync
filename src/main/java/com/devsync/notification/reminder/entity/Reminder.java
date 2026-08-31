package com.devsync.notification.reminder.entity;

import com.devsync.team.entity.Team;
import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReminderType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String message;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    @Column(nullable = false, length = 50)
    private String timezone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Reminder() {
    }

    public Reminder(UUID id, User user, Team team, ReminderType type, String title, String message, LocalTime reminderTime, String timezone, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.team = team;
        this.type = type;
        this.title = title;
        this.message = message;
        this.reminderTime = reminderTime;
        this.timezone = timezone;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
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

    public static ReminderBuilder builder() {
        return new ReminderBuilder();
    }

    public static class ReminderBuilder {
        private UUID id;
        private User user;
        private Team team;
        private ReminderType type;
        private String title;
        private String message;
        private LocalTime reminderTime;
        private String timezone;
        private boolean active = true;
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

        public ReminderBuilder active(boolean active) {
            this.active = active;
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
            return new Reminder(id, user, team, type, title, message, reminderTime, timezone, active, createdAt, updatedAt);
        }
    }
}
