package com.devsync.goal.dto;

import com.devsync.goal.entity.GoalPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public class CreateGoalRequest {

    @NotNull(message = "Owner ID is required")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID ownerId;

    @NotNull(message = "Team ID is required")
    @Schema(example = "987e6543-e21b-12d3-a456-426614174000")
    private UUID teamId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "Master Spring Boot & Cloud Architecture")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(example = "Comprehensive goal covering Spring Data, Security, Flyway, and Microservices")
    private String description;

    @Schema(example = "2026-09-01")
    private LocalDate startDate;

    @Schema(example = "2026-12-31")
    private LocalDate targetDate;

    @NotNull(message = "Priority is required")
    @Schema(example = "HIGH")
    private GoalPriority priority;

    public CreateGoalRequest() {
    }

    public CreateGoalRequest(UUID ownerId, UUID teamId, String title, String description, LocalDate startDate, LocalDate targetDate, GoalPriority priority) {
        this.ownerId = ownerId;
        this.teamId = teamId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.priority = priority;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
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

    public static CreateGoalRequestBuilder builder() {
        return new CreateGoalRequestBuilder();
    }

    public static class CreateGoalRequestBuilder {
        private UUID ownerId;
        private UUID teamId;
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate targetDate;
        private GoalPriority priority = GoalPriority.MEDIUM;

        public CreateGoalRequestBuilder ownerId(UUID ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public CreateGoalRequestBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public CreateGoalRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CreateGoalRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateGoalRequestBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public CreateGoalRequestBuilder targetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
            return this;
        }

        public CreateGoalRequestBuilder priority(GoalPriority priority) {
            this.priority = priority;
            return this;
        }

        public CreateGoalRequest build() {
            return new CreateGoalRequest(ownerId, teamId, title, description, startDate, targetDate, priority);
        }
    }
}
