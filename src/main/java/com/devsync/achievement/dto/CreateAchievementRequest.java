package com.devsync.achievement.dto;

import com.devsync.achievement.entity.AchievementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class CreateAchievementRequest {

    @NotNull(message = "User ID is required")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "7-Day Coding Streak")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(example = "Completed daily progress submissions for 7 consecutive days")
    private String description;

    @NotNull(message = "Achievement type is required")
    @Schema(example = "STREAK")
    private AchievementType type;

    @Size(max = 255, message = "Icon must not exceed 255 characters")
    @Schema(example = "flame")
    private String icon;

    @NotNull(message = "Points are required")
    @Min(value = 0, message = "Points must be non-negative")
    @Schema(example = "100")
    private Integer points;

    @NotNull(message = "Earned timestamp is required")
    @Schema(example = "2026-09-01T12:00:00Z")
    private Instant earnedAt;

    public CreateAchievementRequest() {
    }

    public CreateAchievementRequest(UUID userId, String title, String description, AchievementType type, String icon, Integer points, Instant earnedAt) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.icon = icon;
        this.points = points;
        this.earnedAt = earnedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public static CreateAchievementRequestBuilder builder() {
        return new CreateAchievementRequestBuilder();
    }

    public static class CreateAchievementRequestBuilder {
        private UUID userId;
        private String title;
        private String description;
        private AchievementType type;
        private String icon;
        private Integer points = 0;
        private Instant earnedAt;

        public CreateAchievementRequestBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public CreateAchievementRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CreateAchievementRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateAchievementRequestBuilder type(AchievementType type) {
            this.type = type;
            return this;
        }

        public CreateAchievementRequestBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public CreateAchievementRequestBuilder points(Integer points) {
            this.points = points;
            return this;
        }

        public CreateAchievementRequestBuilder earnedAt(Instant earnedAt) {
            this.earnedAt = earnedAt;
            return this;
        }

        public CreateAchievementRequest build() {
            return new CreateAchievementRequest(userId, title, description, type, icon, points, earnedAt);
        }
    }
}
