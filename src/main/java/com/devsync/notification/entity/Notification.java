package com.devsync.notification.entity;

import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Notification() {
    }

    public Notification(UUID id, User user, NotificationType type, NotificationChannel channel, NotificationStatus status, String title, String message, UUID referenceId, String referenceType, Instant scheduledAt, Instant sentAt, Instant readAt, boolean deleted, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.channel = channel != null ? channel : NotificationChannel.IN_APP;
        this.status = status != null ? status : NotificationStatus.PENDING;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.readAt = readAt;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.channel == null) {
            this.channel = NotificationChannel.IN_APP;
        }
        if (this.status == null) {
            this.status = NotificationStatus.PENDING;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public static class NotificationBuilder {
        private UUID id;
        private User user;
        private NotificationType type;
        private NotificationChannel channel = NotificationChannel.IN_APP;
        private NotificationStatus status = NotificationStatus.PENDING;
        private String title;
        private String message;
        private UUID referenceId;
        private String referenceType;
        private Instant scheduledAt;
        private Instant sentAt;
        private Instant readAt;
        private boolean deleted = false;
        private Instant createdAt;
        private Instant updatedAt;

        public NotificationBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public NotificationBuilder user(User user) {
            this.user = user;
            return this;
        }

        public NotificationBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public NotificationBuilder channel(NotificationChannel channel) {
            this.channel = channel;
            return this;
        }

        public NotificationBuilder status(NotificationStatus status) {
            this.status = status;
            return this;
        }

        public NotificationBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationBuilder message(String message) {
            this.message = message;
            return this;
        }

        public NotificationBuilder referenceId(UUID referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public NotificationBuilder referenceType(String referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        public NotificationBuilder scheduledAt(Instant scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public NotificationBuilder sentAt(Instant sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public NotificationBuilder readAt(Instant readAt) {
            this.readAt = readAt;
            return this;
        }

        public NotificationBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public NotificationBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NotificationBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Notification build() {
            return new Notification(id, user, type, channel, status, title, message, referenceId, referenceType, scheduledAt, sentAt, readAt, deleted, createdAt, updatedAt);
        }
    }
}
