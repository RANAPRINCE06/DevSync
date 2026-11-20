package com.devsync.coding.dto;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;

import java.time.Instant;
import java.util.UUID;

public class PlatformSyncStatusResponse {

    private UUID accountId;
    private CodingPlatform platform;
    private SyncStatus syncStatus;
    private Instant lastSyncedAt;
    private Instant lastSyncAttemptAt;
    private String lastSyncError;

    public PlatformSyncStatusResponse() {
    }

    public PlatformSyncStatusResponse(UUID accountId, CodingPlatform platform, SyncStatus syncStatus,
                                      Instant lastSyncedAt, Instant lastSyncAttemptAt, String lastSyncError) {
        this.accountId = accountId;
        this.platform = platform;
        this.syncStatus = syncStatus;
        this.lastSyncedAt = lastSyncedAt;
        this.lastSyncAttemptAt = lastSyncAttemptAt;
        this.lastSyncError = lastSyncError;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
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
}
