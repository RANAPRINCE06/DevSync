package com.devsync.goal.dto;

import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class GoalResponse {

    private UUID id;
    private UUID ownerId;
    private String ownerName;
    private String ownerEmail;
    private UUID teamId;
    private String teamName;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate targetDate;
    private GoalStatus status;
    private GoalPriority priority;
    private Integer progressPercentage;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public GoalResponse() {
    }

    public GoalResponse(UUID id, UUID ownerId, String ownerName, String ownerEmail, UUID teamId, String teamName, String title, String description, LocalDate startDate, LocalDate targetDate, GoalStatus status, GoalPriority priority, Integer progressPercentage, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.teamId = teamId;
        this.teamName = teamName;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.status = status;
        this.priority = priority;
        this.progressPercentage = progressPercentage;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GoalResponse fromEntity(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .ownerId(goal.getOwner().getId())
                .ownerName(goal.getOwner().getName())
                .ownerEmail(goal.getOwner().getEmail())
                .teamId(goal.getTeam().getId())
                .teamName(goal.getTeam().getName())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .startDate(goal.getStartDate())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .priority(goal.getPriority())
                .progressPercentage(goal.getProgressPercentage())
                .active(goal.isActive())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setUserEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
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

    public static GoalResponseBuilder builder() {
        return new GoalResponseBuilder();
    }

    public static class GoalResponseBuilder {
        private UUID id;
        private UUID ownerId;
        private String ownerName;
        private String ownerEmail;
        private UUID teamId;
        private String teamName;
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate targetDate;
        private GoalStatus status;
        private GoalPriority priority;
        private Integer progressPercentage;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;

        public GoalResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public GoalResponseBuilder ownerId(UUID ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public GoalResponseBuilder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public GoalResponseBuilder ownerEmail(String ownerEmail) {
            this.ownerEmail = ownerEmail;
            return this;
        }

        public GoalResponseBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public GoalResponseBuilder teamName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public GoalResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public GoalResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public GoalResponseBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public GoalResponseBuilder targetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
            return this;
        }

        public GoalResponseBuilder status(GoalStatus status) {
            this.status = status;
            return this;
        }

        public GoalResponseBuilder priority(GoalPriority priority) {
            this.priority = priority;
            return this;
        }

        public GoalResponseBuilder progressPercentage(Integer progressPercentage) {
            this.progressPercentage = progressPercentage;
            return this;
        }

        public GoalResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public GoalResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GoalResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public GoalResponse build() {
            return new GoalResponse(id, ownerId, ownerName, ownerEmail, teamId, teamName, title, description, startDate, targetDate, status, priority, progressPercentage, active, createdAt, updatedAt);
        }
    }
}
