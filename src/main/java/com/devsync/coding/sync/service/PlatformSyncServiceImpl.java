package com.devsync.coding.sync.service;

import com.devsync.coding.dto.PlatformAccountResponse;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.mapper.CodingProfileMapper;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.coding.sync.dto.PlatformSyncResult;
import com.devsync.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates automated and manual platform synchronization, tracking in-flight accounts
 * to prevent duplicate concurrent runs and isolating platform failures.
 */
@Service
public class PlatformSyncServiceImpl implements PlatformSyncService {

    private static final Logger log = LoggerFactory.getLogger(PlatformSyncServiceImpl.class);

    private final PlatformSyncWorker platformSyncWorker;
    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository platformStatSnapshotRepository;
    private final Set<UUID> inFlightAccountIds = ConcurrentHashMap.newKeySet();

    public PlatformSyncServiceImpl(PlatformSyncWorker platformSyncWorker,
                                   PlatformAccountRepository platformAccountRepository,
                                   PlatformStatSnapshotRepository platformStatSnapshotRepository) {
        this.platformSyncWorker = platformSyncWorker;
        this.platformAccountRepository = platformAccountRepository;
        this.platformStatSnapshotRepository = platformStatSnapshotRepository;
    }

    @Override
    public PlatformSyncResult syncAccount(PlatformAccount account) {
        if (account == null || account.getId() == null) {
            throw new IllegalArgumentException("Platform account must not be null");
        }

        if (!account.isActive()) {
            log.info("Account [{}] is disconnected/inactive; skipping synchronization", account.getId());
            return PlatformSyncResult.builder()
                    .accountId(account.getId())
                    .platform(account.getPlatform())
                    .username(account.getUsername())
                    .status(account.getSyncStatus())
                    .snapshotCreated(false)
                    .inFlightSkipped(false)
                    .timestamp(Instant.now())
                    .errorMessage("Account is disconnected/inactive")
                    .build();
        }

        // Prevent simultaneous duplicate synchronization of the same account
        if (!inFlightAccountIds.add(account.getId())) {
            log.warn("Synchronization for account [{}] is already in progress; skipping duplicate attempt",
                    account.getId());
            return PlatformSyncResult.builder()
                    .accountId(account.getId())
                    .platform(account.getPlatform())
                    .username(account.getUsername())
                    .status(account.getSyncStatus())
                    .snapshotCreated(false)
                    .inFlightSkipped(true)
                    .timestamp(Instant.now())
                    .errorMessage("Synchronization already in progress")
                    .build();
        }

        try {
            return platformSyncWorker.executeSync(account);
        } finally {
            inFlightAccountIds.remove(account.getId());
        }
    }

    @Override
    public List<PlatformSyncResult> syncAllActiveAccounts() {
        List<PlatformAccount> activeAccounts = platformAccountRepository.findAllByActiveTrue();
        log.info("Automated synchronization started for {} active platform account(s)", activeAccounts.size());

        List<PlatformSyncResult> results = new ArrayList<>();
        for (PlatformAccount account : activeAccounts) {
            try {
                PlatformSyncResult result = syncAccount(account);
                results.add(result);
            } catch (Exception ex) {
                log.error("Unhandled exception during synchronization of account [{}]: {}",
                        account.getId(), ex.getMessage(), ex);
                results.add(PlatformSyncResult.builder()
                        .accountId(account.getId())
                        .platform(account.getPlatform())
                        .username(account.getUsername())
                        .status(SyncStatus.FAILED)
                        .snapshotCreated(false)
                        .inFlightSkipped(false)
                        .timestamp(Instant.now())
                        .errorMessage("Unhandled error: " + ex.getMessage())
                        .build());
            }
        }

        long successCount = results.stream().filter(r -> r.getStatus() == SyncStatus.SUCCESS).count();
        long failCount = results.stream().filter(r -> r.getStatus() == SyncStatus.FAILED).count();
        log.info("Automated synchronization cycle completed: total={}, success={}, failed={}",
                results.size(), successCount, failCount);

        return results;
    }

    @Override
    @Transactional
    public PlatformAccountResponse syncAccountForUser(UUID accountId, UUID userId) {
        PlatformAccount account = platformAccountRepository.findByIdAndUserIdAndActiveTrue(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found: " + accountId));

        syncAccount(account);

        PlatformAccount refreshed = platformAccountRepository.findById(accountId)
                .orElse(account);
        PlatformStatSnapshot latest = platformStatSnapshotRepository
                .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(accountId)
                .orElse(null);

        return CodingProfileMapper.toResponse(refreshed, latest);
    }

    @Override
    public boolean isAccountSyncInFlight(UUID accountId) {
        return accountId != null && inFlightAccountIds.contains(accountId);
    }
}
