package com.devsync.goal.dto;

import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UpdateGoalRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "Master Spring Boot & Cloud Architecture (Updated)")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(example = "Updated description with new milestones")
    private String description;

    @Schema(example = "2026-09-01")
    private LocalDate startDate;

    @Schema(example = "2026-12-31")
    private LocalDate targetDate;

    @NotNull(message = "Priority is required")
    @Schema(example = "CRITICAL")
    private GoalPriority priority;

    @NotNull(message = "Progress percentage is required")
    @Min(value = 0, message = "Progress percentage must be at least 0")
    @Max(value = 100, message = "Progress percentage cannot exceed 100")
    @Schema(example = "50")
    private Integer progressPercentage;

    @NotNull(message = "Status is required")
    @Schema(example = "IN_PROGRESS")
    private GoalStatus status;

    public UpdateGoalRequest() {
    }

    public UpdateGoalRequest(String title, String description, LocalDate startDate, LocalDate targetDate, GoalPriority priority, Integer progressPercentage, GoalStatus status) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.priority = priority;
        this.progressPercentage = progressPercentage;
        this.status = status;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public GoalPriority getPriority() {
        return priority;
    }

    public void setPriority(GoalPriority priority) {
        this.priority = priority;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public static UpdateGoalRequestBuilder builder() {
        return new UpdateGoalRequestBuilder();
    }

    public static class UpdateGoalRequestBuilder {
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate targetDate;
        private GoalPriority priority = GoalPriority.MEDIUM;
        private Integer progressPercentage = 0;
        private GoalStatus status = GoalStatus.NOT_STARTED;

        public UpdateGoalRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public UpdateGoalRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UpdateGoalRequestBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public UpdateGoalRequestBuilder targetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
            return this;
        }

        public UpdateGoalRequestBuilder priority(GoalPriority priority) {
            this.priority = priority;
            return this;
        }

        public UpdateGoalRequestBuilder progressPercentage(Integer progressPercentage) {
            this.progressPercentage = progressPercentage;
            return this;
        }

        public UpdateGoalRequestBuilder status(GoalStatus status) {
            this.status = status;
            return this;
        }

        public UpdateGoalRequest build() {
            return new UpdateGoalRequest(title, description, startDate, targetDate, priority, progressPercentage, status);
        }
    }
}
