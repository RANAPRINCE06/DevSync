package com.devsync.progress.dto;

import com.devsync.progress.entity.DailyProgress;
import com.devsync.progress.entity.ProgressStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ProgressResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID teamId;
    private String teamName;
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

    public ProgressResponse() {
    }

    public ProgressResponse(UUID id, UUID userId, String userName, String userEmail, UUID teamId, String teamName, LocalDate progressDate, String whatStudied, String completed, Integer studyMinutes, String challenges, String improvementAreas, String tomorrowPlan, ProgressStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.teamId = teamId;
        this.teamName = teamName;
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

    public static ProgressResponse fromEntity(DailyProgress progress) {
        return ProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .userName(progress.getUser().getName())
                .userEmail(progress.getUser().getEmail())
                .teamId(progress.getTeam().getId())
                .teamName(progress.getTeam().getName())
                .progressDate(progress.getProgressDate())
                .whatStudied(progress.getWhatStudied())
                .completed(progress.getCompleted())
                .studyMinutes(progress.getStudyMinutes())
                .challenges(progress.getChallenges())
                .improvementAreas(progress.getImprovementAreas())
                .tomorrowPlan(progress.getTomorrowPlan())
                .status(progress.getStatus())
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public static ProgressResponseBuilder builder() {
        return new ProgressResponseBuilder();
    }

    public static class ProgressResponseBuilder {
        private UUID id;
        private UUID userId;
        private String userName;
        private String userEmail;
        private UUID teamId;
        private String teamName;
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

        public ProgressResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ProgressResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public ProgressResponseBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public ProgressResponseBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public ProgressResponseBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public ProgressResponseBuilder teamName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public ProgressResponseBuilder progressDate(LocalDate progressDate) {
            this.progressDate = progressDate;
            return this;
        }

        public ProgressResponseBuilder whatStudied(String whatStudied) {
            this.whatStudied = whatStudied;
            return this;
        }

        public ProgressResponseBuilder completed(String completed) {
            this.completed = completed;
            return this;
        }

        public ProgressResponseBuilder studyMinutes(Integer studyMinutes) {
            this.studyMinutes = studyMinutes;
            return this;
        }

        public ProgressResponseBuilder challenges(String challenges) {
            this.challenges = challenges;
            return this;
        }

        public ProgressResponseBuilder improvementAreas(String improvementAreas) {
            this.improvementAreas = improvementAreas;
            return this;
        }

        public ProgressResponseBuilder tomorrowPlan(String tomorrowPlan) {
            this.tomorrowPlan = tomorrowPlan;
            return this;
        }

        public ProgressResponseBuilder status(ProgressStatus status) {
            this.status = status;
            return this;
        }

        public ProgressResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ProgressResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ProgressResponse build() {
            return new ProgressResponse(id, userId, userName, userEmail, teamId, teamName, progressDate, whatStudied, completed, studyMinutes, challenges, improvementAreas, tomorrowPlan, status, createdAt, updatedAt);
        }
    }
}
