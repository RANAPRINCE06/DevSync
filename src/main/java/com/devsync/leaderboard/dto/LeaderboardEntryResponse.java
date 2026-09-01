package com.devsync.leaderboard.dto;

import java.util.UUID;

public class LeaderboardEntryResponse {

    private int rank;
    private UUID userId;
    private String userName;
    private UUID teamId;
    private String teamName;
    private long score;
    private long progressEntries;
    private long completedTasks;
    private long completedGoals;
    private long achievementPoints;

    public LeaderboardEntryResponse() {
    }

    public LeaderboardEntryResponse(int rank, UUID userId, String userName, UUID teamId, String teamName, long score, long progressEntries, long completedTasks, long completedGoals, long achievementPoints) {
        this.rank = rank;
        this.userId = userId;
        this.userName = userName;
        this.teamId = teamId;
        this.teamName = teamName;
        this.score = score;
        this.progressEntries = progressEntries;
        this.completedTasks = completedTasks;
        this.completedGoals = completedGoals;
        this.achievementPoints = achievementPoints;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public long getProgressEntries() {
        return progressEntries;
    }

    public void setProgressEntries(long progressEntries) {
        this.progressEntries = progressEntries;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getCompletedGoals() {
        return completedGoals;
    }

    public void setCompletedGoals(long completedGoals) {
        this.completedGoals = completedGoals;
    }

    public long getAchievementPoints() {
        return achievementPoints;
    }

    public void setAchievementPoints(long achievementPoints) {
        this.achievementPoints = achievementPoints;
    }

    public static LeaderboardEntryResponseBuilder builder() {
        return new LeaderboardEntryResponseBuilder();
    }

    public static class LeaderboardEntryResponseBuilder {
        private int rank;
        private UUID userId;
        private String userName;
        private UUID teamId;
        private String teamName;
        private long score;
        private long progressEntries;
        private long completedTasks;
        private long completedGoals;
        private long achievementPoints;

        public LeaderboardEntryResponseBuilder rank(int rank) {
            this.rank = rank;
            return this;
        }

        public LeaderboardEntryResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public LeaderboardEntryResponseBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public LeaderboardEntryResponseBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public LeaderboardEntryResponseBuilder teamName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public LeaderboardEntryResponseBuilder score(long score) {
            this.score = score;
            return this;
        }

        public LeaderboardEntryResponseBuilder progressEntries(long progressEntries) {
            this.progressEntries = progressEntries;
            return this;
        }

        public LeaderboardEntryResponseBuilder completedTasks(long completedTasks) {
            this.completedTasks = completedTasks;
            return this;
        }

        public LeaderboardEntryResponseBuilder completedGoals(long completedGoals) {
            this.completedGoals = completedGoals;
            return this;
        }

        public LeaderboardEntryResponseBuilder achievementPoints(long achievementPoints) {
            this.achievementPoints = achievementPoints;
            return this;
        }

        public LeaderboardEntryResponse build() {
            return new LeaderboardEntryResponse(rank, userId, userName, teamId, teamName, score, progressEntries, completedTasks, completedGoals, achievementPoints);
        }
    }
}
