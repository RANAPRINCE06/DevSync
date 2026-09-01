package com.devsync.achievement.entity;

import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AchievementType type;

    @Column(length = 255)
    private String icon;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(name = "earned_at", nullable = false)
    private Instant earnedAt;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Achievement() {
    }

    public Achievement(UUID id, User user, String title, String description, AchievementType type, String icon, Integer points, Instant earnedAt, Boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.type = type;
        this.icon = icon;
        this.points = points != null ? points : 0;
        this.earnedAt = earnedAt;
        this.active = active != null ? active : true;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.points == null) {
            this.points = 0;
        }
        if (this.active == null) {
            this.active = true;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AchievementType getType() {
        return type;
    }

    public void setType(AchievementType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Instant getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(Instant earnedAt) {
        this.earnedAt = earnedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
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

    public static AchievementBuilder builder() {
        return new AchievementBuilder();
    }

    public static class AchievementBuilder {
        private UUID id;
        private User user;
        private String title;
        private String description;
        private AchievementType type;
        private String icon;
        private Integer points = 0;
        private Instant earnedAt;
        private Boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public AchievementBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public AchievementBuilder user(User user) {
            this.user = user;
            return this;
        }

        public AchievementBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AchievementBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AchievementBuilder type(AchievementType type) {
            this.type = type;
            return this;
        }

        public AchievementBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public AchievementBuilder points(Integer points) {
            this.points = points;
            return this;
        }

        public AchievementBuilder earnedAt(Instant earnedAt) {
            this.earnedAt = earnedAt;
            return this;
        }

        public AchievementBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public AchievementBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AchievementBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Achievement build() {
            return new Achievement(id, user, title, description, type, icon, points, earnedAt, active, createdAt, updatedAt);
        }
    }
}
