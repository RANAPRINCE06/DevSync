package com.devsync.coding.dto;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.PlatformCapability;
import com.devsync.coding.enums.SyncStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class PlatformAccountResponse {

    private UUID id;
    private CodingPlatform platform;
    private String platformDisplayName;
    private String username;
    private String profileUrl;
    private boolean active;
    private SyncStatus syncStatus;
    private Instant lastSyncedAt;
    private Instant lastSyncAttemptAt;
    private String lastSyncError;
    private Instant createdAt;
    private Instant updatedAt;
    private PlatformStatSnapshotResponse latestSnapshot;
    private Set<PlatformCapability> capabilities;

    public PlatformAccountResponse() {
    }

    public PlatformAccountResponse(UUID id, CodingPlatform platform, String platformDisplayName, String username,
                                    String profileUrl, boolean active, SyncStatus syncStatus, Instant lastSyncedAt,
                                    Instant lastSyncAttemptAt, String lastSyncError, Instant createdAt,
                                    Instant updatedAt, PlatformStatSnapshotResponse latestSnapshot) {
        this(id, platform, platformDisplayName, username, profileUrl, active, syncStatus, lastSyncedAt,
                lastSyncAttemptAt, lastSyncError, createdAt, updatedAt, latestSnapshot,
                platform != null ? platform.getCapabilities() : Set.of());
    }

    public PlatformAccountResponse(UUID id, CodingPlatform platform, String platformDisplayName, String username,
                                   String profileUrl, boolean active, SyncStatus syncStatus, Instant lastSyncedAt,
                                   Instant lastSyncAttemptAt, String lastSyncError, Instant createdAt,
                                   Instant updatedAt, PlatformStatSnapshotResponse latestSnapshot,
                                   Set<PlatformCapability> capabilities) {
        this.id = id;
        this.platform = platform;
        this.platformDisplayName = platformDisplayName;
        this.username = username;
        this.profileUrl = profileUrl;
        this.active = active;
        this.syncStatus = syncStatus;
        this.lastSyncedAt = lastSyncedAt;
        this.lastSyncAttemptAt = lastSyncAttemptAt;
        this.lastSyncError = lastSyncError;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.latestSnapshot = latestSnapshot;
        this.capabilities = capabilities;
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

    public PlatformStatSnapshotResponse getLatestSnapshot() {
        return latestSnapshot;
    }

    public void setLatestSnapshot(PlatformStatSnapshotResponse latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
    }

    public Set<PlatformCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<PlatformCapability> capabilities) {
        this.capabilities = capabilities;
    }
}
