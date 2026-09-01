package com.devsync.achievement.dto;

import com.devsync.achievement.entity.AchievementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class UpdateAchievementRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "Updated Streak Achievement")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(example = "Updated description for achievement")
    private String description;

    @NotNull(message = "Achievement type is required")
    @Schema(example = "STREAK")
    private AchievementType type;

    @Size(max = 255, message = "Icon must not exceed 255 characters")
    @Schema(example = "flame")
    private String icon;

    @NotNull(message = "Points are required")
    @Min(value = 0, message = "Points must be non-negative")
    @Schema(example = "150")
    private Integer points;

    @NotNull(message = "Earned timestamp is required")
    @Schema(example = "2026-09-01T12:00:00Z")
    private Instant earnedAt;

    @NotNull(message = "Active status is required")
    @Schema(example = "true")
    private Boolean active;

    public UpdateAchievementRequest() {
    }

    public UpdateAchievementRequest(String title, String description, AchievementType type, String icon, Integer points, Instant earnedAt, Boolean active) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.icon = icon;
        this.points = points;
        this.earnedAt = earnedAt;
        this.active = active;
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

    public static UpdateAchievementRequestBuilder builder() {
        return new UpdateAchievementRequestBuilder();
    }

    public static class UpdateAchievementRequestBuilder {
        private String title;
        private String description;
        private AchievementType type;
        private String icon;
        private Integer points = 0;
        private Instant earnedAt;
        private Boolean active = true;

        public UpdateAchievementRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public UpdateAchievementRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UpdateAchievementRequestBuilder type(AchievementType type) {
            this.type = type;
            return this;
        }

        public UpdateAchievementRequestBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public UpdateAchievementRequestBuilder points(Integer points) {
            this.points = points;
            return this;
        }

        public UpdateAchievementRequestBuilder earnedAt(Instant earnedAt) {
            this.earnedAt = earnedAt;
            return this;
        }

        public UpdateAchievementRequestBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public UpdateAchievementRequest build() {
            return new UpdateAchievementRequest(title, description, type, icon, points, earnedAt, active);
        }
    }
}
