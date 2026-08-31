package com.devsync.task.dto;

import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(example = "Implement Spring Security JWT filter (Updated)")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(example = "Updated implementation details")
    private String description;

    @NotNull(message = "Priority is required")
    @Schema(example = "HIGH")
    private TaskPriority priority;

    @Schema(example = "2026-09-20")
    private LocalDate dueDate;

    @NotNull(message = "Status is required")
    @Schema(example = "IN_PROGRESS")
    private TaskStatus status;

    @Min(value = 0, message = "Actual minutes cannot be negative")
    @Max(value = 1440, message = "Actual minutes cannot exceed 1440 minutes (24 hours)")
    @Schema(example = "120")
    private Integer actualMinutes;

    public UpdateTaskRequest() {
    }

    public UpdateTaskRequest(String title, String description, TaskPriority priority, LocalDate dueDate, TaskStatus status, Integer actualMinutes) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.status = status;
        this.actualMinutes = actualMinutes;
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getActualMinutes() {
        return actualMinutes;
    }

    public void setActualMinutes(Integer actualMinutes) {
        this.actualMinutes = actualMinutes;
    }

    public static UpdateTaskRequestBuilder builder() {
        return new UpdateTaskRequestBuilder();
    }

    public static class UpdateTaskRequestBuilder {
        private String title;
        private String description;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private LocalDate dueDate;
        private TaskStatus status = TaskStatus.TODO;
        private Integer actualMinutes = 0;

        public UpdateTaskRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public UpdateTaskRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UpdateTaskRequestBuilder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public UpdateTaskRequestBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public UpdateTaskRequestBuilder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public UpdateTaskRequestBuilder actualMinutes(Integer actualMinutes) {
            this.actualMinutes = actualMinutes;
            return this;
        }

        public UpdateTaskRequest build() {
            return new UpdateTaskRequest(title, description, priority, dueDate, status, actualMinutes);
        }
    }
}
