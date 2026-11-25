package com.devsync.coding.intelligence.dto;

import com.devsync.coding.enums.CodingPlatform;
import java.util.List;
import java.util.Map;

public class PlatformContestHistoryResponse {

    private CodingPlatform platform;
    private String platformDisplayName;
    private Double currentRating;
    private Double highestRating;
    private Integer totalContests;
    private List<ContestRecordResponse> contests;
    private Map<CodingPlatform, List<ContestRecordResponse>> allPlatformsContests;

    public PlatformContestHistoryResponse() {
    }

    public PlatformContestHistoryResponse(CodingPlatform platform, String platformDisplayName,
                                          Double currentRating, Double highestRating,
                                          Integer totalContests, List<ContestRecordResponse> contests,
                                          Map<CodingPlatform, List<ContestRecordResponse>> allPlatformsContests) {
        this.platform = platform;
        this.platformDisplayName = platformDisplayName;
        this.currentRating = currentRating;
        this.highestRating = highestRating;
        this.totalContests = totalContests;
        this.contests = contests;
        this.allPlatformsContests = allPlatformsContests;
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
    }

    public String getPlatformDisplayName() {
        return platformDisplayName;
    }

    public void setPlatformDisplayName(String platformDisplayName) {
        this.platformDisplayName = platformDisplayName;
    }

    public Double getCurrentRating() {
        return currentRating;
    }

    public void setCurrentRating(Double currentRating) {
        this.currentRating = currentRating;
    }

    public Double getHighestRating() {
        return highestRating;
    }

    public void setHighestRating(Double highestRating) {
        this.highestRating = highestRating;
    }

    public Integer getTotalContests() {
        return totalContests;
    }

    public void setTotalContests(Integer totalContests) {
        this.totalContests = totalContests;
    }

    public List<ContestRecordResponse> getContests() {
        return contests;
    }

    public void setContests(List<ContestRecordResponse> contests) {
        this.contests = contests;
    }

    public Map<CodingPlatform, List<ContestRecordResponse>> getAllPlatformsContests() {
        return allPlatformsContests;
    }

    public void setAllPlatformsContests(Map<CodingPlatform, List<ContestRecordResponse>> allPlatformsContests) {
        this.allPlatformsContests = allPlatformsContests;
    }
}
