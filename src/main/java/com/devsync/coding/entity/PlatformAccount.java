package com.devsync.coding.entity;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "platform_accounts")
public class PlatformAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CodingPlatform platform;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "profile_url", nullable = false, length = 500)
    private String profileUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 30)
    private SyncStatus syncStatus = SyncStatus.NEVER_SYNCED;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "last_sync_attempt_at")
    private Instant lastSyncAttemptAt;

    @Column(name = "last_sync_error", length = 1000)
    private String lastSyncError;

    @OneToMany(mappedBy = "platformAccount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("snapshotAt DESC")
    private List<PlatformStatSnapshot> snapshots = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PlatformAccount() {
    }

    public PlatformAccount(UUID id, User user, CodingPlatform platform, String username, String profileUrl,
                           boolean active, SyncStatus syncStatus, Instant lastSyncedAt, Instant lastSyncAttemptAt,
                           String lastSyncError, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.active = active;
        this.syncStatus = syncStatus != null ? syncStatus : SyncStatus.NEVER_SYNCED;
        this.lastSyncedAt = lastSyncedAt;
        this.lastSyncAttemptAt = lastSyncAttemptAt;
        this.lastSyncError = lastSyncError;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.syncStatus == null) {
            this.syncStatus = SyncStatus.NEVER_SYNCED;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Instant getLastSyncAttemptAt() {
        return lastSyncAttemptAt;
    }

    public void setLastSyncAttemptAt(Instant lastSyncAttemptAt) {
        this.lastSyncAttemptAt = lastSyncAttemptAt;
    }

    public String getLastSyncError() {
        return lastSyncError;
    }

    public void setLastSyncError(String lastSyncError) {
        this.lastSyncError = lastSyncError;
    }

    public List<PlatformStatSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<PlatformStatSnapshot> snapshots) {
        this.snapshots = snapshots;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private CodingPlatform platform;
        private String username;
        private String profileUrl;
        private boolean active = true;
        private SyncStatus syncStatus = SyncStatus.NEVER_SYNCED;
        private Instant lastSyncedAt;
        private Instant lastSyncAttemptAt;
        private String lastSyncError;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder platform(CodingPlatform platform) {
            this.platform = platform;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder profileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder syncStatus(SyncStatus syncStatus) {
            this.syncStatus = syncStatus;
            return this;
        }

        public Builder lastSyncedAt(Instant lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
            return this;
        }

        public Builder lastSyncAttemptAt(Instant lastSyncAttemptAt) {
            this.lastSyncAttemptAt = lastSyncAttemptAt;
            return this;
        }

        public Builder lastSyncError(String lastSyncError) {
            this.lastSyncError = lastSyncError;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PlatformAccount build() {
            return new PlatformAccount(id, user, platform, username, profileUrl, active, syncStatus,
                    lastSyncedAt, lastSyncAttemptAt, lastSyncError, createdAt, updatedAt);
        }
    }
}
