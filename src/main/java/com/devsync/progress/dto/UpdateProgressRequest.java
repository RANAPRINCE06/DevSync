package com.devsync.progress.dto;

import com.devsync.progress.entity.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class UpdateProgressRequest {

    @NotBlank(message = "What studied/worked on is required")
    @Size(max = 2000, message = "What studied must not exceed 2000 characters")
    @Schema(example = "Updated learning details for today")
    private String whatStudied;

    @NotBlank(message = "Completed tasks summary is required")
    @Size(max = 2000, message = "Completed tasks summary must not exceed 2000 characters")
    @Schema(example = "Updated completed items list")
    private String completed;

    @NotNull(message = "Study minutes is required")
    @Min(value = 0, message = "Study minutes cannot be negative")
    @Max(value = 1440, message = "Study minutes cannot exceed 1440 minutes (24 hours)")
    @Schema(example = "240")
    private Integer studyMinutes;

    @Size(max = 2000, message = "Challenges description must not exceed 2000 characters")
    @Schema(example = "Resolved JPA query performance issue")
    private String challenges;

    @Size(max = 2000, message = "Improvement areas must not exceed 2000 characters")
    @Schema(example = "Added unit test coverage for edge cases")
    private String improvementAreas;

    @Size(max = 2000, message = "Tomorrow's plan must not exceed 2000 characters")
    @Schema(example = "Start working on upcoming features")
    private String tomorrowPlan;

    @NotNull(message = "Progress status is required")
    @Schema(example = "COMPLETED")
    private ProgressStatus status;

    public UpdateProgressRequest() {
    }

    public UpdateProgressRequest(String whatStudied, String completed, Integer studyMinutes, String challenges, String improvementAreas, String tomorrowPlan, ProgressStatus status) {
        this.whatStudied = whatStudied;
        this.completed = completed;
        this.studyMinutes = studyMinutes;
        this.challenges = challenges;
        this.improvementAreas = improvementAreas;
        this.tomorrowPlan = tomorrowPlan;
        this.status = status;
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

    public static UpdateProgressRequestBuilder builder() {
        return new UpdateProgressRequestBuilder();
    }

    public static class UpdateProgressRequestBuilder {
        private String whatStudied;
        private String completed;
        private Integer studyMinutes;
        private String challenges;
        private String improvementAreas;
        private String tomorrowPlan;
        private ProgressStatus status;

        public UpdateProgressRequestBuilder whatStudied(String whatStudied) {
            this.whatStudied = whatStudied;
            return this;
        }

        public UpdateProgressRequestBuilder completed(String completed) {
            this.completed = completed;
            return this;
        }

        public UpdateProgressRequestBuilder studyMinutes(Integer studyMinutes) {
            this.studyMinutes = studyMinutes;
            return this;
        }

        public UpdateProgressRequestBuilder challenges(String challenges) {
            this.challenges = challenges;
            return this;
        }

        public UpdateProgressRequestBuilder improvementAreas(String improvementAreas) {
            this.improvementAreas = improvementAreas;
            return this;
        }

        public UpdateProgressRequestBuilder tomorrowPlan(String tomorrowPlan) {
            this.tomorrowPlan = tomorrowPlan;
            return this;
        }

        public UpdateProgressRequestBuilder status(ProgressStatus status) {
            this.status = status;
            return this;
        }

        public UpdateProgressRequest build() {
            return new UpdateProgressRequest(whatStudied, completed, studyMinutes, challenges, improvementAreas, tomorrowPlan, status);
        }
    }
}
