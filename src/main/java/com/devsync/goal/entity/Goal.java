package com.devsync.goal.entity;

import com.devsync.team.entity.Team;
import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoalStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoalPriority priority;

    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Goal() {
    }

    public Goal(UUID id, User owner, Team team, String title, String description, LocalDate startDate, LocalDate targetDate, GoalStatus status, GoalPriority priority, Integer progressPercentage, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.owner = owner;
        this.team = team;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.status = status != null ? status : GoalStatus.NOT_STARTED;
        this.priority = priority != null ? priority : GoalPriority.MEDIUM;
        this.progressPercentage = progressPercentage != null ? progressPercentage : 0;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = GoalStatus.NOT_STARTED;
        }
        if (this.priority == null) {
            this.priority = GoalPriority.MEDIUM;
        }
        if (this.progressPercentage == null) {
            this.progressPercentage = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
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

    public static GoalBuilder builder() {
        return new GoalBuilder();
    }

    public static class GoalBuilder {
        private UUID id;
        private User owner;
        private Team team;
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate targetDate;
        private GoalStatus status = GoalStatus.NOT_STARTED;
        private GoalPriority priority = GoalPriority.MEDIUM;
        private Integer progressPercentage = 0;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public GoalBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public GoalBuilder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public GoalBuilder team(Team team) {
            this.team = team;
            return this;
        }

        public GoalBuilder title(String title) {
            this.title = title;
            return this;
        }

        public GoalBuilder description(String description) {
            this.description = description;
            return this;
        }

        public GoalBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public GoalBuilder targetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
            return this;
        }

        public GoalBuilder status(GoalStatus status) {
            this.status = status;
            return this;
        }

        public GoalBuilder priority(GoalPriority priority) {
            this.priority = priority;
            return this;
        }

        public GoalBuilder progressPercentage(Integer progressPercentage) {
            this.progressPercentage = progressPercentage;
            return this;
        }

        public GoalBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public GoalBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GoalBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Goal build() {
            return new Goal(id, owner, team, title, description, startDate, targetDate, status, priority, progressPercentage, active, createdAt, updatedAt);
        }
    }
}
