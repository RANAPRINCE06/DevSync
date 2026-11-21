package com.devsync.coding.sync.service;

import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformRateLimitException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import com.devsync.coding.integration.registry.PlatformIntegrationRegistry;
import com.devsync.coding.reliability.cache.PlatformProfileCache;
import com.devsync.coding.reliability.metrics.CodingSyncMetrics;
import com.devsync.coding.reliability.ratelimit.PlatformRateLimitManager;
import com.devsync.coding.reliability.retry.PlatformSyncRetryExecutor;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.coding.sync.dto.PlatformSyncResult;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Executes a single platform account synchronization lifecycle:
 * PENDING -> SUCCESS or PENDING -> FAILED.
 * Enforces rate limiting, in-memory profile caching, retry with exponential backoff,
 * duplicate snapshot suppression, failure isolation, and operational metrics.
 */
@Component
public class PlatformSyncWorker {

    private static final Logger log = LoggerFactory.getLogger(PlatformSyncWorker.class);

    private final PlatformIntegrationRegistry platformIntegrationRegistry;
    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository platformStatSnapshotRepository;
    private final PlatformRateLimitManager platformRateLimitManager;
    private final PlatformProfileCache platformProfileCache;
    private final PlatformSyncRetryExecutor platformSyncRetryExecutor;
    private final CodingSyncMetrics codingSyncMetrics;

    @Autowired
    public PlatformSyncWorker(PlatformIntegrationRegistry platformIntegrationRegistry,
                              PlatformAccountRepository platformAccountRepository,
                              PlatformStatSnapshotRepository platformStatSnapshotRepository,
                              PlatformRateLimitManager platformRateLimitManager,
                              PlatformProfileCache platformProfileCache,
                              PlatformSyncRetryExecutor platformSyncRetryExecutor,
                              CodingSyncMetrics codingSyncMetrics) {
        this.platformIntegrationRegistry = platformIntegrationRegistry;
        this.platformAccountRepository = platformAccountRepository;
        this.platformStatSnapshotRepository = platformStatSnapshotRepository;
        this.platformRateLimitManager = platformRateLimitManager;
        this.platformProfileCache = platformProfileCache;
        this.platformSyncRetryExecutor = platformSyncRetryExecutor;
        this.codingSyncMetrics = codingSyncMetrics;
    }

    /**
     * Executes sync lifecycle with default scheduled trigger type.
     */
    @Transactional
    public PlatformSyncResult executeSync(PlatformAccount account) {
        return executeSync(account, "scheduled");
    }

    /**
     * Executes the end-to-end sync lifecycle for an individual platform account.
     *
     * @param account the platform account to synchronize
     * @param triggerType origin of trigger ("scheduled" or "manual")
     * @return PlatformSyncResult indicating success/failure and snapshot creation
     */
    @Transactional
    public PlatformSyncResult executeSync(PlatformAccount account, String triggerType) {
        if (!account.isActive()) {
            log.info("Skipping inactive/disconnected platform account: {}", account.getId());
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

        Timer.Sample timer = codingSyncMetrics != null ? codingSyncMetrics.startTimer() : null;
        if (codingSyncMetrics != null) {
            codingSyncMetrics.recordAttempt(account.getPlatform(), triggerType);
        }

        Instant attemptTime = Instant.now();
        account.setSyncStatus(SyncStatus.PENDING);
        account.setLastSyncAttemptAt(attemptTime);
        platformAccountRepository.saveAndFlush(account);

        log.info("Starting synchronization for platform account [{}] platform={} username={} trigger={}",
                account.getId(), account.getPlatform(), account.getUsername(), triggerType);

        try {
            CodingPlatformAdapter adapter = platformIntegrationRegistry.getRequiredAdapter(account.getPlatform());

            // 1. Check local profile cache
            ExternalProfileData profileData = null;
            if (platformProfileCache != null && platformProfileCache.isEnabled()) {
                profileData = platformProfileCache.get(account.getPlatform(), account.getUsername());
                if (profileData != null && codingSyncMetrics != null) {
                    codingSyncMetrics.recordCacheHit(account.getPlatform());
                } else if (codingSyncMetrics != null) {
                    codingSyncMetrics.recordCacheMiss(account.getPlatform());
                }
            }

            // 2. Cache miss: check rate limits and query adapter with retry policy
            if (profileData == null) {
                if (platformRateLimitManager != null && !platformRateLimitManager.tryAcquire(account.getPlatform())) {
                    long waitSec = platformRateLimitManager.getEstimatedWaitTimeSeconds(account.getPlatform());
                    if (codingSyncMetrics != null) {
                        codingSyncMetrics.recordRateLimited(account.getPlatform());
                    }
                    throw new PlatformRateLimitException(
                            "Rate limit reached for " + account.getPlatform() + ". Retry after " + waitSec + "s",
                            waitSec
                    );
                }

                if (platformSyncRetryExecutor != null) {
                    profileData = platformSyncRetryExecutor.executeWithRetry(
                            account.getPlatform(),
                            account.getUsername(),
                            () -> adapter.fetchProfileData(account.getUsername())
                    );
                } else {
                    profileData = adapter.fetchProfileData(account.getUsername());
                }

                if (profileData == null) {
                    throw new PlatformIntegrationException("Platform adapter returned null profile data for " + account.getUsername());
                }

                // Cache newly fetched external profile data
                if (platformProfileCache != null && platformProfileCache.isEnabled()) {
                    platformProfileCache.put(account.getPlatform(), account.getUsername(), profileData);
                }
            }

            // 3. Snapshot duplicate detection
            Optional<PlatformStatSnapshot> latestSnapshotOpt = platformStatSnapshotRepository
                    .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

            boolean snapshotCreated = false;
            if (latestSnapshotOpt.isEmpty() || !isSameStatistics(latestSnapshotOpt.get(), profileData)) {
                PlatformStatSnapshot newSnapshot = PlatformStatSnapshot.builder()
                        .platformAccount(account)
                        .currentRating(profileData.getCurrentRating())
                        .maximumRating(profileData.getMaximumRating())
                        .globalRank(profileData.getGlobalRank())
                        .countryRank(profileData.getCountryRank())
                        .problemsSolved(profileData.getProblemsSolved())
                        .easySolved(profileData.getEasySolved())
                        .mediumSolved(profileData.getMediumSolved())
                        .hardSolved(profileData.getHardSolved())
                        .contestsParticipated(profileData.getContestsParticipated())
                        .additionalStats(profileData.getAdditionalStats())
                        .snapshotAt(profileData.getFetchedAt() != null ? profileData.getFetchedAt() : Instant.now())
                        .build();

                platformStatSnapshotRepository.saveAndFlush(newSnapshot);
                snapshotCreated = true;
                log.info("Persisted fresh statistics snapshot for account [{}] (snapshotCreated=true)", account.getId());
            } else {
                log.info("Statistics unchanged for account [{}]; duplicate snapshot suppressed", account.getId());
            }

            account.setSyncStatus(SyncStatus.SUCCESS);
            account.setLastSyncedAt(Instant.now());
            account.setLastSyncError(null);
            platformAccountRepository.saveAndFlush(account);

            if (codingSyncMetrics != null) {
                codingSyncMetrics.recordSuccess(account.getPlatform());
                codingSyncMetrics.stopTimer(timer, account.getPlatform(), "success");
            }

            return PlatformSyncResult.builder()
                    .accountId(account.getId())
                    .platform(account.getPlatform())
                    .username(account.getUsername())
                    .status(SyncStatus.SUCCESS)
                    .snapshotCreated(snapshotCreated)
                    .inFlightSkipped(false)
                    .timestamp(Instant.now())
                    .errorMessage(null)
                    .build();

        } catch (Exception ex) {
            String sanitizedError = sanitizeErrorMessage(ex.getMessage());
            String errorType = classifyErrorType(ex);

            log.warn("Synchronization failed for account [{}] platform={}: {}",
                    account.getId(), account.getPlatform(), sanitizedError);

            if (codingSyncMetrics != null) {
                codingSyncMetrics.recordFailure(account.getPlatform(), errorType);
                codingSyncMetrics.stopTimer(timer, account.getPlatform(), "failure");
            }

            account.setSyncStatus(SyncStatus.FAILED);
            account.setLastSyncError(sanitizedError);
            platformAccountRepository.saveAndFlush(account);

            return PlatformSyncResult.builder()
                    .accountId(account.getId())
                    .platform(account.getPlatform())
                    .username(account.getUsername())
                    .status(SyncStatus.FAILED)
                    .snapshotCreated(false)
                    .inFlightSkipped(false)
                    .timestamp(Instant.now())
                    .errorMessage(sanitizedError)
                    .build();
        }
    }

    private String classifyErrorType(Throwable ex) {
        if (ex instanceof PlatformRateLimitException) return "rate_limit";
        if (ex instanceof PlatformUserNotFoundException) return "not_found";
        if (ex instanceof PlatformUnsupportedException) return "unsupported";
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("timed out") || msg.contains("timeout")) return "timeout";
        if (msg.contains("server unavailable") || msg.contains("http 5")) return "server_error";
        return "integration_error";
    }

    private boolean isSameStatistics(PlatformStatSnapshot s, ExternalProfileData d) {
        boolean sameStatsMap = isSameMap(s.getAdditionalStats(), d.getAdditionalStats());
        return Objects.equals(s.getCurrentRating(), d.getCurrentRating()) &&
                Objects.equals(s.getMaximumRating(), d.getMaximumRating()) &&
                Objects.equals(s.getGlobalRank(), d.getGlobalRank()) &&
                Objects.equals(s.getCountryRank(), d.getCountryRank()) &&
                Objects.equals(s.getProblemsSolved(), d.getProblemsSolved()) &&
                Objects.equals(s.getEasySolved(), d.getEasySolved()) &&
                Objects.equals(s.getMediumSolved(), d.getMediumSolved()) &&
                Objects.equals(s.getHardSolved(), d.getHardSolved()) &&
                Objects.equals(s.getContestsParticipated(), d.getContestsParticipated()) &&
                sameStatsMap;
    }

    private boolean isSameMap(Map<String, Object> m1, Map<String, Object> m2) {
        boolean m1Empty = (m1 == null || m1.isEmpty());
        boolean m2Empty = (m2 == null || m2.isEmpty());
        if (m1Empty && m2Empty) {
            return true;
        }
        return Objects.equals(m1, m2);
    }

    public String sanitizeErrorMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Unknown platform synchronization error";
        }
        String sanitized = message.replaceAll("(?i)(password|secret|token|key|authorization)=[^\s&]+", "$1=***");
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 497) + "...";
        }
        return sanitized;
    }
}
