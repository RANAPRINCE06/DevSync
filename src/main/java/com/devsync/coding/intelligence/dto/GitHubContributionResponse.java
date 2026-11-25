package com.devsync.coding.intelligence.dto;

import java.util.List;

public class GitHubContributionResponse {
    private int publicRepos;
    private int totalStars;
    private int totalContributions;
    private int activeDays;
    private int currentStreak;
    private int longestStreak;
    private List<DailyContributionPoint> dailyContributions;

    public GitHubContributionResponse() {}

    public GitHubContributionResponse(int publicRepos, int totalStars, int totalContributions,
                                      int activeDays, int currentStreak, int longestStreak,
                                      List<DailyContributionPoint> dailyContributions) {
        this.publicRepos = publicRepos;
        this.totalStars = totalStars;
        this.totalContributions = totalContributions;
        this.activeDays = activeDays;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.dailyContributions = dailyContributions;
    }

    public int getPublicRepos() { return publicRepos; }
    public void setPublicRepos(int publicRepos) { this.publicRepos = publicRepos; }
    public int getTotalStars() { return totalStars; }
    public void setTotalStars(int totalStars) { this.totalStars = totalStars; }
    public int getTotalContributions() { return totalContributions; }
    public void setTotalContributions(int totalContributions) { this.totalContributions = totalContributions; }
    public int getActiveDays() { return activeDays; }
    public void setActiveDays(int activeDays) { this.activeDays = activeDays; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    public List<DailyContributionPoint> getDailyContributions() { return dailyContributions; }
    public void setDailyContributions(List<DailyContributionPoint> dailyContributions) { this.dailyContributions = dailyContributions; }

    public static class DailyContributionPoint {
        private String date;
        private int count;
        private int intensity; // 0 to 4

        public DailyContributionPoint() {}

        public DailyContributionPoint(String date, int count, int intensity) {
            this.date = date;
            this.count = count;
            this.intensity = intensity;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public int getIntensity() { return intensity; }
        public void setIntensity(int intensity) { this.intensity = intensity; }
    }
}
