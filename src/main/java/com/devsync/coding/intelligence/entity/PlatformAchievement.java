package com.devsync.coding.intelligence.entity;

import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.enums.CodingPlatform;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "platform_achievements")
public class PlatformAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_account_id", nullable = false)
    private PlatformAccount platformAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private CodingPlatform platform;

    @Column(name = "achievement_id", nullable = false, length = 150)
    private String achievementId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "level", length = 50)
    private String level;

    @Column(name = "earned_date")
    private Instant earnedDate;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "last_verified_at", nullable = false)
    private Instant lastVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PlatformAchievement() {
    }

    public PlatformAchievement(UUID id, PlatformAccount platformAccount, CodingPlatform platform,
                               String achievementId, String name, String description, String imageUrl,
                               String category, String level, Instant earnedDate, String sourceUrl,
                               Instant lastVerifiedAt, Instant createdAt) {
        this.id = id;
        this.platformAccount = platformAccount;
        this.platform = platform;
        this.achievementId = achievementId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.level = level;
        this.earnedDate = earnedDate;
        this.sourceUrl = sourceUrl;
        this.lastVerifiedAt = lastVerifiedAt != null ? lastVerifiedAt : Instant.now();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.lastVerifiedAt == null) {
            this.lastVerifiedAt = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlatformAccount getPlatformAccount() {
        return platformAccount;
    }

    public void setPlatformAccount(PlatformAccount platformAccount) {
        this.platformAccount = platformAccount;
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
