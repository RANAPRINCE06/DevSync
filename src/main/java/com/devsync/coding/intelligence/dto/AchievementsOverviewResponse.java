package com.devsync.coding.intelligence.dto;

import com.devsync.coding.enums.CodingPlatform;
import java.util.List;
import java.util.Map;

public class AchievementsOverviewResponse {
    private int totalAchievements;
    private Map<CodingPlatform, Integer> platformCounts;
    private List<PlatformAchievementResponse> recentlyEarned;
    private List<PlatformAchievementResponse> achievements;

    public AchievementsOverviewResponse() {}

    public AchievementsOverviewResponse(int totalAchievements, Map<CodingPlatform, Integer> platformCounts,
                                        List<PlatformAchievementResponse> recentlyEarned,
                                        List<PlatformAchievementResponse> achievements) {
        this.totalAchievements = totalAchievements;
        this.platformCounts = platformCounts;
        this.recentlyEarned = recentlyEarned;
        this.achievements = achievements;
    }

    public int getTotalAchievements() { return totalAchievements; }
    public void setTotalAchievements(int totalAchievements) { this.totalAchievements = totalAchievements; }
    public Map<CodingPlatform, Integer> getPlatformCounts() { return platformCounts; }
    public void setPlatformCounts(Map<CodingPlatform, Integer> platformCounts) { this.platformCounts = platformCounts; }
    public List<PlatformAchievementResponse> getRecentlyEarned() { return recentlyEarned; }
    public void setRecentlyEarned(List<PlatformAchievementResponse> recentlyEarned) { this.recentlyEarned = recentlyEarned; }
    public List<PlatformAchievementResponse> getAchievements() { return achievements; }
    public void setAchievements(List<PlatformAchievementResponse> achievements) { this.achievements = achievements; }
}
