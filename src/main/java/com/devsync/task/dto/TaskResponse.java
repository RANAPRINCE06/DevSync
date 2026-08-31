package com.devsync.task.dto;

import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TaskResponse {

    private UUID id;
    private UUID goalId;
    private String goalTitle;
    private UUID assigneeId;
    private String assigneeName;
    private String assigneeEmail;
    private UUID teamId;
    private String teamName;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private Integer estimatedMinutes;
    private Integer actualMinutes;
    private Instant completedAt;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public TaskResponse() {
    }

    public TaskResponse(UUID id, UUID goalId, String goalTitle, UUID assigneeId, String assigneeName, String assigneeEmail, UUID teamId, String teamName, String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate, Integer estimatedMinutes, Integer actualMinutes, Instant completedAt, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.goalId = goalId;
        this.goalTitle = goalTitle;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.assigneeEmail = assigneeEmail;
        this.teamId = teamId;
        this.teamName = teamName;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.estimatedMinutes = estimatedMinutes;
        this.actualMinutes = actualMinutes;
        this.completedAt = completedAt;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TaskResponse fromEntity(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .goalId(task.getGoal().getId())
                .goalTitle(task.getGoal().getTitle())
                .assigneeId(task.getAssignee().getId())
                .assigneeName(task.getAssignee().getName())
                .assigneeEmail(task.getAssignee().getEmail())
                .teamId(task.getTeam().getId())
                .teamName(task.getTeam().getName())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .estimatedMinutes(task.getEstimatedMinutes())
                .actualMinutes(task.getActualMinutes())
                .completedAt(task.getCompletedAt())
                .active(task.isActive())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public void setGoalId(UUID goalId) {
        this.goalId = goalId;
    }

    public String getGoalTitle() {
        return goalTitle;
    }

    public void setGoalTitle(String goalTitle) {
        this.goalTitle = goalTitle;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getAssigneeEmail() {
        return assigneeEmail;
    }

    public void setAssigneeEmail(String assigneeEmail) {
        this.assigneeEmail = assigneeEmail;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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

    public Integer getActualMinutes() {
        return actualMinutes;
    }

    public void setActualMinutes(Integer actualMinutes) {
        this.actualMinutes = actualMinutes;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
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

    public static TaskResponseBuilder builder() {
        return new TaskResponseBuilder();
    }

    public static class TaskResponseBuilder {
        private UUID id;
        private UUID goalId;
        private String goalTitle;
        private UUID assigneeId;
        private String assigneeName;
        private String assigneeEmail;
        private UUID teamId;
        private String teamName;
        private String title;
        private String description;
        private TaskStatus status;
        private TaskPriority priority;
        private LocalDate dueDate;
        private Integer estimatedMinutes;
        private Integer actualMinutes;
        private Instant completedAt;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;

        public TaskResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TaskResponseBuilder goalId(UUID goalId) {
            this.goalId = goalId;
            return this;
        }

        public TaskResponseBuilder goalTitle(String goalTitle) {
            this.goalTitle = goalTitle;
            return this;
        }

        public TaskResponseBuilder assigneeId(UUID assigneeId) {
            this.assigneeId = assigneeId;
            return this;
        }

        public TaskResponseBuilder assigneeName(String assigneeName) {
            this.assigneeName = assigneeName;
            return this;
        }

        public TaskResponseBuilder assigneeEmail(String assigneeEmail) {
            this.assigneeEmail = assigneeEmail;
            return this;
        }

        public TaskResponseBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public TaskResponseBuilder teamName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public TaskResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TaskResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TaskResponseBuilder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public TaskResponseBuilder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public TaskResponseBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public TaskResponseBuilder estimatedMinutes(Integer estimatedMinutes) {
            this.estimatedMinutes = estimatedMinutes;
            return this;
        }

        public TaskResponseBuilder actualMinutes(Integer actualMinutes) {
            this.actualMinutes = actualMinutes;
            return this;
        }

        public TaskResponseBuilder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public TaskResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public TaskResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TaskResponse build() {
            return new TaskResponse(id, goalId, goalTitle, assigneeId, assigneeName, assigneeEmail, teamId, teamName, title, description, status, priority, dueDate, estimatedMinutes, actualMinutes, completedAt, active, createdAt, updatedAt);
        }
    }
}
