package com.devsync.coding.mapper;

import com.devsync.coding.dto.PlatformAccountResponse;
import com.devsync.coding.dto.PlatformStatSnapshotResponse;
import com.devsync.coding.dto.PlatformSyncStatusResponse;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;

public final class CodingProfileMapper {

    private CodingProfileMapper() {
    }

    public static PlatformAccountResponse toResponse(PlatformAccount account, PlatformStatSnapshot latestSnapshot) {
        if (account == null) {
            return null;
        }
        return new PlatformAccountResponse(
                account.getId(),
                account.getPlatform(),
                account.getPlatform() != null ? account.getPlatform().getDisplayName() : null,
                account.getUsername(),
                account.getProfileUrl(),
                account.isActive(),
                account.getSyncStatus(),
                account.getLastSyncedAt(),
                account.getLastSyncAttemptAt(),
                account.getLastSyncError(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                toSnapshotResponse(latestSnapshot)
        );
    }

    public static PlatformStatSnapshotResponse toSnapshotResponse(PlatformStatSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new PlatformStatSnapshotResponse(
                snapshot.getId(),
                snapshot.getCurrentRating(),
                snapshot.getMaximumRating(),
                snapshot.getGlobalRank(),
                snapshot.getCountryRank(),
                snapshot.getProblemsSolved(),
                snapshot.getEasySolved(),
                snapshot.getMediumSolved(),
                snapshot.getHardSolved(),
                snapshot.getContestsParticipated(),
                snapshot.getAdditionalStats(),
                snapshot.getSnapshotAt()
        );
    }

    public static PlatformSyncStatusResponse toSyncStatusResponse(PlatformAccount account) {
        if (account == null) {
            return null;
        }
        return new PlatformSyncStatusResponse(
                account.getId(),
                account.getPlatform(),
                account.getSyncStatus(),
                account.getLastSyncedAt(),
                account.getLastSyncAttemptAt(),
                account.getLastSyncError()
        );
    }
}
