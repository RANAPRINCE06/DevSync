package com.devsync.coding.intelligence.dto;

import com.devsync.coding.enums.CodingPlatform;
import java.time.Instant;
import java.util.UUID;

public class PlatformAchievementResponse {

    private UUID id;
    private CodingPlatform platform;
    private String platformDisplayName;
    private String achievementId;
    private String name;
    private String description;
    private String imageUrl;
    private String category;
    private String level;
    private Instant earnedDate;
    private String sourceUrl;
    private Instant lastVerifiedAt;

    public PlatformAchievementResponse() {
    }

    public PlatformAchievementResponse(UUID id, CodingPlatform platform, String platformDisplayName,
                                       String achievementId, String name, String description,
                                       String imageUrl, String category, String level,
                                       Instant earnedDate, String sourceUrl, Instant lastVerifiedAt) {
        this.id = id;
        this.platform = platform;
        this.platformDisplayName = platformDisplayName;
        this.achievementId = achievementId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.level = level;
        this.earnedDate = earnedDate;
        this.sourceUrl = sourceUrl;
        this.lastVerifiedAt = lastVerifiedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(String achievementId) {
        this.achievementId = achievementId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Instant getEarnedDate() {
        return earnedDate;
    }

    public void setEarnedDate(Instant earnedDate) {
        this.earnedDate = earnedDate;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Instant getLastVerifiedAt() {
        return lastVerifiedAt;
    }

    public void setLastVerifiedAt(Instant lastVerifiedAt) {
        this.lastVerifiedAt = lastVerifiedAt;
    }
}
