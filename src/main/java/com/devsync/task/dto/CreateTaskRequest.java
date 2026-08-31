package com.devsync.task.dto;

import com.devsync.task.entity.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public class CreateTaskRequest {

    @NotNull(message = "Goal ID is required")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID goalId;

    @NotNull(message = "Assignee ID is required")
    @Schema(example = "234e5678-e89b-12d3-a456-426614174000")
    private UUID assigneeId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "Implement Spring Security JWT filter")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(example = "Create custom OncePerRequestFilter and TokenProvider")
    private String description;

    @NotNull(message = "Priority is required")
    @Schema(example = "HIGH")
    private TaskPriority priority;

    @Schema(example = "2026-09-15")
    private LocalDate dueDate;

    @Min(value = 0, message = "Estimated minutes cannot be negative")
    @Max(value = 1440, message = "Estimated minutes cannot exceed 1440 minutes (24 hours)")
    @Schema(example = "180")
    private Integer estimatedMinutes;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(UUID goalId, UUID assigneeId, String title, String description, TaskPriority priority, LocalDate dueDate, Integer estimatedMinutes) {
        this.goalId = goalId;
        this.assigneeId = assigneeId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.estimatedMinutes = estimatedMinutes;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public void setGoalId(UUID goalId) {
        this.goalId = goalId;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
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

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public static CreateTaskRequestBuilder builder() {
        return new CreateTaskRequestBuilder();
    }

    public static class CreateTaskRequestBuilder {
        private UUID goalId;
        private UUID assigneeId;
        private String title;
        private String description;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private LocalDate dueDate;
        private Integer estimatedMinutes;

        public CreateTaskRequestBuilder goalId(UUID goalId) {
            this.goalId = goalId;
            return this;
        }

        public CreateTaskRequestBuilder assigneeId(UUID assigneeId) {
            this.assigneeId = assigneeId;
            return this;
        }

        public CreateTaskRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CreateTaskRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateTaskRequestBuilder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public CreateTaskRequestBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public CreateTaskRequestBuilder estimatedMinutes(Integer estimatedMinutes) {
            this.estimatedMinutes = estimatedMinutes;
            return this;
        }

        public CreateTaskRequest build() {
            return new CreateTaskRequest(goalId, assigneeId, title, description, priority, dueDate, estimatedMinutes);
        }
    }
}
