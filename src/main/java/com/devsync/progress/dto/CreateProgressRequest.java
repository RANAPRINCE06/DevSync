package com.devsync.progress.dto;

import com.devsync.progress.entity.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public class CreateProgressRequest {

    @NotNull(message = "User ID is required")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotNull(message = "Team ID is required")
    @Schema(example = "987e6543-e21b-12d3-a456-426614174000")
    private UUID teamId;

    @NotNull(message = "Progress date is required")
    @Schema(example = "2026-08-28")
    private LocalDate progressDate;

    @NotBlank(message = "What studied/worked on is required")
    @Size(max = 2000, message = "What studied must not exceed 2000 characters")
    @Schema(example = "Studied Spring Data JPA Specifications and Flyway migrations")
    private String whatStudied;

    @NotBlank(message = "Completed tasks summary is required")
    @Size(max = 2000, message = "Completed tasks summary must not exceed 2000 characters")
    @Schema(example = "Completed Step 2A entity mapping and bug fixes")
    private String completed;

    @NotNull(message = "Study minutes is required")
    @Min(value = 0, message = "Study minutes cannot be negative")
    @Max(value = 1440, message = "Study minutes cannot exceed 1440 minutes (24 hours)")
    @Schema(example = "180")
    private Integer studyMinutes;

    @Size(max = 2000, message = "Challenges description must not exceed 2000 characters")
    @Schema(example = "Understanding JPA Specification compositions for date filtering")
    private String challenges;

    @Size(max = 2000, message = "Improvement areas must not exceed 2000 characters")
    @Schema(example = "Write cleaner integration test assertions")
    private String improvementAreas;

    @Size(max = 2000, message = "Tomorrow's plan must not exceed 2000 characters")
    @Schema(example = "Implement daily progress leaderboard calculations")
    private String tomorrowPlan;

    @NotNull(message = "Progress status is required")
    @Schema(example = "COMPLETED")
    private ProgressStatus status;

    public CreateProgressRequest() {
    }

    public CreateProgressRequest(UUID userId, UUID teamId, LocalDate progressDate, String whatStudied, String completed, Integer studyMinutes, String challenges, String improvementAreas, String tomorrowPlan, ProgressStatus status) {
        this.userId = userId;
        this.teamId = teamId;
        this.progressDate = progressDate;
        this.whatStudied = whatStudied;
        this.completed = completed;
        this.studyMinutes = studyMinutes;
        this.challenges = challenges;
        this.improvementAreas = improvementAreas;
        this.tomorrowPlan = tomorrowPlan;
        this.status = status;
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

    public LocalDate getProgressDate() {
        return progressDate;
    }

    public void setProgressDate(LocalDate progressDate) {
        this.progressDate = progressDate;
    }

    public String getWhatStudied() {
        return whatStudied;
    }

    public void setWhatStudied(String whatStudied) {
        this.whatStudied = whatStudied;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public Integer getStudyMinutes() {
        return studyMinutes;
    }

    public void setStudyMinutes(Integer studyMinutes) {
        this.studyMinutes = studyMinutes;
    }

    public String getChallenges() {
        return challenges;
    }

    public void setChallenges(String challenges) {
        this.challenges = challenges;
    }

    public String getImprovementAreas() {
        return improvementAreas;
    }

    public void setImprovementAreas(String improvementAreas) {
        this.improvementAreas = improvementAreas;
    }

    public String getTomorrowPlan() {
        return tomorrowPlan;
    }

    public void setTomorrowPlan(String tomorrowPlan) {
        this.tomorrowPlan = tomorrowPlan;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
    }

    public static CreateProgressRequestBuilder builder() {
        return new CreateProgressRequestBuilder();
    }

    public static class CreateProgressRequestBuilder {
        private UUID userId;
        private UUID teamId;
        private LocalDate progressDate;
        private String whatStudied;
        private String completed;
        private Integer studyMinutes;
        private String challenges;
        private String improvementAreas;
        private String tomorrowPlan;
        private ProgressStatus status;

        public CreateProgressRequestBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public CreateProgressRequestBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public CreateProgressRequestBuilder progressDate(LocalDate progressDate) {
            this.progressDate = progressDate;
            return this;
        }

        public CreateProgressRequestBuilder whatStudied(String whatStudied) {
            this.whatStudied = whatStudied;
            return this;
        }

        public CreateProgressRequestBuilder completed(String completed) {
            this.completed = completed;
            return this;
        }

        public CreateProgressRequestBuilder studyMinutes(Integer studyMinutes) {
            this.studyMinutes = studyMinutes;
            return this;
        }

        public CreateProgressRequestBuilder challenges(String challenges) {
            this.challenges = challenges;
            return this;
        }

        public CreateProgressRequestBuilder improvementAreas(String improvementAreas) {
            this.improvementAreas = improvementAreas;
            return this;
        }

        public CreateProgressRequestBuilder tomorrowPlan(String tomorrowPlan) {
            this.tomorrowPlan = tomorrowPlan;
            return this;
        }

        public CreateProgressRequestBuilder status(ProgressStatus status) {
            this.status = status;
            return this;
        }

        public CreateProgressRequest build() {
            return new CreateProgressRequest(userId, teamId, progressDate, whatStudied, completed, studyMinutes, challenges, improvementAreas, tomorrowPlan, status);
        }
    }
}
