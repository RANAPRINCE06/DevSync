package com.devsync.achievement.dto;

import com.devsync.achievement.entity.Achievement;
import com.devsync.achievement.entity.AchievementType;

import java.time.Instant;
import java.util.UUID;

public class AchievementResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String title;
    private String description;
    private AchievementType type;
    private String icon;
    private Integer points;
    private Instant earnedAt;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public AchievementResponse() {
    }

    public AchievementResponse(UUID id, UUID userId, String userName, String userEmail, String title, String description, AchievementType type, String icon, Integer points, Instant earnedAt, Boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.title = title;
        this.description = description;
        this.type = type;
        this.icon = icon;
        this.points = points;
        this.earnedAt = earnedAt;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AchievementResponse fromEntity(Achievement achievement) {
        return AchievementResponse.builder()
                .id(achievement.getId())
                .userId(achievement.getUser().getId())
                .userName(achievement.getUser().getName())
                .userEmail(achievement.getUser().getEmail())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .type(achievement.getType())
                .icon(achievement.getIcon())
                .points(achievement.getPoints())
                .earnedAt(achievement.getEarnedAt())
                .active(achievement.getActive())
                .createdAt(achievement.getCreatedAt())
                .updatedAt(achievement.getUpdatedAt())
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

    public static AchievementResponseBuilder builder() {
        return new AchievementResponseBuilder();
    }

    public static class AchievementResponseBuilder {
        private UUID id;
        private UUID userId;
        private String userName;
        private String userEmail;
        private String title;
        private String description;
        private AchievementType type;
        private String icon;
        private Integer points = 0;
        private Instant earnedAt;
        private Boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public AchievementResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public AchievementResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public AchievementResponseBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public AchievementResponseBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public AchievementResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AchievementResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AchievementResponseBuilder type(AchievementType type) {
            this.type = type;
            return this;
        }

        public AchievementResponseBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public AchievementResponseBuilder points(Integer points) {
            this.points = points;
            return this;
        }

        public AchievementResponseBuilder earnedAt(Instant earnedAt) {
            this.earnedAt = earnedAt;
            return this;
        }

        public AchievementResponseBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public AchievementResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AchievementResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AchievementResponse build() {
            return new AchievementResponse(id, userId, userName, userEmail, title, description, type, icon, points, earnedAt, active, createdAt, updatedAt);
        }
    }
}
