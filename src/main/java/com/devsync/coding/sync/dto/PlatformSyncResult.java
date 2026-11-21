package com.devsync.coding.sync.dto;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Result outcome of an automated or manual platform synchronization attempt.
 */
@Value
@Builder
public class PlatformSyncResult {
    UUID accountId;
    CodingPlatform platform;
    String username;
    SyncStatus status;
    boolean snapshotCreated;
    boolean inFlightSkipped;
    Instant timestamp;
    String errorMessage;
}
