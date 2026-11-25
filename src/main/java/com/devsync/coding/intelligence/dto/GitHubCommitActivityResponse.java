package com.devsync.coding.intelligence.dto;

import java.util.List;

public class GitHubCommitActivityResponse {
    private int totalCommits;
    private int last7DaysCommits;
    private int last30DaysCommits;
    private int last90DaysCommits;
    private List<DailyCommitPoint> dailyCommits;
    private List<MonthlyCommitPoint> monthlyCommits;
    private List<RepoCommitBreakdown> repoBreakdown;

    public GitHubCommitActivityResponse() {}

    public GitHubCommitActivityResponse(int totalCommits, int last7DaysCommits, int last30DaysCommits,
                                        int last90DaysCommits, List<DailyCommitPoint> dailyCommits,
                                        List<MonthlyCommitPoint> monthlyCommits, List<RepoCommitBreakdown> repoBreakdown) {
        this.totalCommits = totalCommits;
        this.last7DaysCommits = last7DaysCommits;
        this.last30DaysCommits = last30DaysCommits;
        this.last90DaysCommits = last90DaysCommits;
        this.dailyCommits = dailyCommits;
        this.monthlyCommits = monthlyCommits;
        this.repoBreakdown = repoBreakdown;
    }

    public int getTotalCommits() { return totalCommits; }
    public void setTotalCommits(int totalCommits) { this.totalCommits = totalCommits; }
    public int getLast7DaysCommits() { return last7DaysCommits; }
    public void setLast7DaysCommits(int last7DaysCommits) { this.last7DaysCommits = last7DaysCommits; }
    public int getLast30DaysCommits() { return last30DaysCommits; }
    public void setLast30DaysCommits(int last30DaysCommits) { this.last30DaysCommits = last30DaysCommits; }
    public int getLast90DaysCommits() { return last90DaysCommits; }
    public void setLast90DaysCommits(int last90DaysCommits) { this.last90DaysCommits = last90DaysCommits; }
    public List<DailyCommitPoint> getDailyCommits() { return dailyCommits; }
    public void setDailyCommits(List<DailyCommitPoint> dailyCommits) { this.dailyCommits = dailyCommits; }
    public List<MonthlyCommitPoint> getMonthlyCommits() { return monthlyCommits; }
    public void setMonthlyCommits(List<MonthlyCommitPoint> monthlyCommits) { this.monthlyCommits = monthlyCommits; }
    public List<RepoCommitBreakdown> getRepoBreakdown() { return repoBreakdown; }
    public void setRepoBreakdown(List<RepoCommitBreakdown> repoBreakdown) { this.repoBreakdown = repoBreakdown; }

    public static class DailyCommitPoint {
        private String date;
        private int count;

        public DailyCommitPoint() {}
        public DailyCommitPoint(String date, int count) {
            this.date = date;
            this.count = count;
        }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class MonthlyCommitPoint {
        private String month;
        private int count;

        public MonthlyCommitPoint() {}
        public MonthlyCommitPoint(String month, int count) {
            this.month = month;
            this.count = count;
        }
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class RepoCommitBreakdown {
        private String repoName;
        private int commits;

        public RepoCommitBreakdown() {}
        public RepoCommitBreakdown(String repoName, int commits) {
            this.repoName = repoName;
            this.commits = commits;
        }
        public String getRepoName() { return repoName; }
        public void setRepoName(String repoName) { this.repoName = repoName; }
        public int getCommits() { return commits; }
        public void setCommits(int commits) { this.commits = commits; }
    }
}
