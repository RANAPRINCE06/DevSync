package com.devsync.progress.entity;

import com.devsync.team.entity.Team;
import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_progress", uniqueConstraints = {
    @UniqueConstraint(name = "uk_daily_progress_user_team_date", columnNames = {"user_id", "team_id", "progress_date"})
})
public class DailyProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "progress_date", nullable = false)
    private LocalDate progressDate;

    @Column(name = "what_studied", nullable = false, length = 2000)
    private String whatStudied;

    @Column(nullable = false, length = 2000)
    private String completed;

    @Column(name = "study_minutes", nullable = false)
    private Integer studyMinutes;

    @Column(length = 2000)
    private String challenges;

    @Column(name = "improvement_areas", length = 2000)
    private String improvementAreas;

    @Column(name = "tomorrow_plan", length = 2000)
    private String tomorrowPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProgressStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DailyProgress() {
    }

    public DailyProgress(UUID id, User user, Team team, LocalDate progressDate, String whatStudied, String completed, Integer studyMinutes, String challenges, String improvementAreas, String tomorrowPlan, ProgressStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.team = team;
        this.progressDate = progressDate;
        this.whatStudied = whatStudied;
        this.completed = completed;
        this.studyMinutes = studyMinutes;
        this.challenges = challenges;
        this.improvementAreas = improvementAreas;
        this.tomorrowPlan = tomorrowPlan;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
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

    public static DailyProgressBuilder builder() {
        return new DailyProgressBuilder();
    }

    public static class DailyProgressBuilder {
        private UUID id;
        private User user;
        private Team team;
        private LocalDate progressDate;
        private String whatStudied;
        private String completed;
        private Integer studyMinutes;
        private String challenges;
        private String improvementAreas;
        private String tomorrowPlan;
        private ProgressStatus status;
        private Instant createdAt;
        private Instant updatedAt;

        public DailyProgressBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public DailyProgressBuilder user(User user) {
            this.user = user;
            return this;
        }

        public DailyProgressBuilder team(Team team) {
            this.team = team;
            return this;
        }

        public DailyProgressBuilder progressDate(LocalDate progressDate) {
            this.progressDate = progressDate;
            return this;
        }

        public DailyProgressBuilder whatStudied(String whatStudied) {
            this.whatStudied = whatStudied;
            return this;
        }

        public DailyProgressBuilder completed(String completed) {
            this.completed = completed;
            return this;
        }

        public DailyProgressBuilder studyMinutes(Integer studyMinutes) {
            this.studyMinutes = studyMinutes;
            return this;
        }

        public DailyProgressBuilder challenges(String challenges) {
            this.challenges = challenges;
            return this;
        }

        public DailyProgressBuilder improvementAreas(String improvementAreas) {
            this.improvementAreas = improvementAreas;
            return this;
        }

        public DailyProgressBuilder tomorrowPlan(String tomorrowPlan) {
            this.tomorrowPlan = tomorrowPlan;
            return this;
        }

        public DailyProgressBuilder status(ProgressStatus status) {
            this.status = status;
            return this;
        }

        public DailyProgressBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DailyProgressBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DailyProgress build() {
            return new DailyProgress(id, user, team, progressDate, whatStudied, completed, studyMinutes, challenges, improvementAreas, tomorrowPlan, status, createdAt, updatedAt);
        }
    }
}
