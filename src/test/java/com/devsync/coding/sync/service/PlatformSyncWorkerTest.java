package com.devsync.coding.sync.service;

import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.registry.PlatformIntegrationRegistry;
import com.devsync.coding.reliability.cache.PlatformProfileCache;
import com.devsync.coding.reliability.metrics.CodingSyncMetrics;
import com.devsync.coding.reliability.ratelimit.PlatformRateLimitManager;
import com.devsync.coding.reliability.retry.PlatformSyncRetryExecutor;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.coding.sync.dto.PlatformSyncResult;
import com.devsync.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformSyncWorkerTest — Unit Tests for Single-Account Sync Lifecycle")
class PlatformSyncWorkerTest {

    @Mock
    private PlatformIntegrationRegistry platformIntegrationRegistry;

    @Mock
    private PlatformAccountRepository platformAccountRepository;

    @Mock
    private PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @Mock
    private CodingPlatformAdapter adapter;

    @Mock
    private PlatformRateLimitManager platformRateLimitManager;

    @Mock
    private PlatformProfileCache platformProfileCache;

    @Mock
    private PlatformSyncRetryExecutor platformSyncRetryExecutor;

    @Mock
    private CodingSyncMetrics codingSyncMetrics;

    @InjectMocks
    private PlatformSyncWorker worker;

    private User user;
    private PlatformAccount account;

    @BeforeEach
    void setUp() throws Exception {
        user = new User();
        user.setId(UUID.randomUUID());

        account = PlatformAccount.builder()
                .id(UUID.randomUUID())
                .user(user)
                .platform(CodingPlatform.LEETCODE)
                .username("test_lc")
                .profileUrl("https://leetcode.com/u/test_lc")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build();

        lenient().when(platformRateLimitManager.tryAcquire(any())).thenReturn(true);
        lenient().when(platformProfileCache.isEnabled()).thenReturn(false);
        lenient().when(platformSyncRetryExecutor.executeWithRetry(any(), any(), any()))
                .thenAnswer(inv -> ((java.util.concurrent.Callable<?>) inv.getArgument(2)).call());
    }

    @Test
    @DisplayName("executeSync — Successful fetch creates snapshot and sets SUCCESS")
    void executeSync_Success_CreatesSnapshot() {
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.LEETCODE)).thenReturn(adapter);
        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId()))
                .thenReturn(Optional.empty());

        ExternalProfileData profileData = ExternalProfileData.builder()
                .platform(CodingPlatform.LEETCODE)
                .username("test_lc")
                .currentRating(1850.5)
                .globalRank(12000L)
                .problemsSolved(450)
                .easySolved(200)
                .mediumSolved(200)
                .hardSolved(50)
                .fetchedAt(Instant.now())
                .build();
        when(adapter.fetchProfileData("test_lc")).thenReturn(profileData);

        PlatformSyncResult result = worker.executeSync(account);

        assertEquals(SyncStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSnapshotCreated());
        assertFalse(result.isInFlightSkipped());
        assertNull(result.getErrorMessage());

        assertEquals(SyncStatus.SUCCESS, account.getSyncStatus());
        assertNotNull(account.getLastSyncedAt());
        assertNull(account.getLastSyncError());

        ArgumentCaptor<PlatformStatSnapshot> snapshotCaptor = ArgumentCaptor.forClass(PlatformStatSnapshot.class);
        verify(platformStatSnapshotRepository).saveAndFlush(snapshotCaptor.capture());
        PlatformStatSnapshot saved = snapshotCaptor.getValue();
        assertEquals(1850.5, saved.getCurrentRating());
        assertEquals(450, saved.getProblemsSolved());
    }

    @Test
    @DisplayName("executeSync — Identical statistics suppresses duplicate snapshot")
    void executeSync_UnchangedData_SuppressesDuplicateSnapshot() {
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.LEETCODE)).thenReturn(adapter);

        PlatformStatSnapshot existing = PlatformStatSnapshot.builder()
                .platformAccount(account)
                .currentRating(1850.5)
                .globalRank(12000L)
                .problemsSolved(450)
                .easySolved(200)
                .mediumSolved(200)
                .hardSolved(50)
                .additionalStats(Map.of())
                .snapshotAt(Instant.now().minusSeconds(3600))
                .build();

        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId()))
                .thenReturn(Optional.of(existing));

        ExternalProfileData profileData = ExternalProfileData.builder()
                .platform(CodingPlatform.LEETCODE)
                .username("test_lc")
                .currentRating(1850.5)
                .globalRank(12000L)
                .problemsSolved(450)
                .easySolved(200)
                .mediumSolved(200)
                .hardSolved(50)
                .additionalStats(Map.of())
                .fetchedAt(Instant.now())
                .build();
        when(adapter.fetchProfileData("test_lc")).thenReturn(profileData);

        PlatformSyncResult result = worker.executeSync(account);

        assertEquals(SyncStatus.SUCCESS, result.getStatus());
        assertFalse(result.isSnapshotCreated(), "Duplicate snapshot should not be persisted");
        verify(platformStatSnapshotRepository, never()).saveAndFlush(any());
        assertEquals(SyncStatus.SUCCESS, account.getSyncStatus());
    }

    @Test
    @DisplayName("executeSync — Inactive/disconnected account is skipped")
    void executeSync_InactiveAccount_Skipped() {
        account.setActive(false);

        PlatformSyncResult result = worker.executeSync(account);

        assertEquals(SyncStatus.NEVER_SYNCED, result.getStatus());
        assertFalse(result.isSnapshotCreated());
        verifyNoInteractions(platformIntegrationRegistry);
        verify(platformStatSnapshotRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("executeSync — Platform failure records sanitized error and preserves existing snapshot")
    void executeSync_Failure_PreservesPreviousSnapshot_SanitizesError() {
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.LEETCODE)).thenReturn(adapter);
        when(adapter.fetchProfileData("test_lc"))
                .thenThrow(new PlatformIntegrationException("External API error token=super_secret_token"));

        PlatformSyncResult result = worker.executeSync(account);

        assertEquals(SyncStatus.FAILED, result.getStatus());
        assertFalse(result.isSnapshotCreated());
        assertTrue(result.getErrorMessage().contains("token=***"), "Sensitive tokens must be sanitized");
        assertFalse(result.getErrorMessage().contains("super_secret_token"));

        assertEquals(SyncStatus.FAILED, account.getSyncStatus());
        assertTrue(account.getLastSyncError().contains("token=***"));
        verify(platformStatSnapshotRepository, never()).delete(any());
        verify(platformStatSnapshotRepository, never()).deleteAll(any());
    }

    @Test
    @DisplayName("executeSync — Null adapter profile data fails gracefully")
    void executeSync_NullProfileData_FailsGracefully() {
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.LEETCODE)).thenReturn(adapter);
        when(adapter.fetchProfileData("test_lc")).thenReturn(null);

        PlatformSyncResult result = worker.executeSync(account);

        assertEquals(SyncStatus.FAILED, result.getStatus());
        assertNotNull(result.getErrorMessage());
        assertEquals(SyncStatus.FAILED, account.getSyncStatus());
    }

    @Test
    @DisplayName("executeSync — Cache hit returns cached data without calling adapter")
    void executeSync_CacheHit_UsesCachedData() {
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.LEETCODE)).thenReturn(adapter);
        when(platformProfileCache.isEnabled()).thenReturn(true);

        ExternalProfileData cachedData = ExternalProfileData.builder()
                .platform(CodingPlatform.LEETCODE)
                .username("test_lc")
                .problemsSolved(300)
                .fetchedAt(Instant.now().minusSeconds(60))
                .build();
        when(platformProfileCache.get(CodingPlatform.LEETCODE, "test_lc")).thenReturn(cachedData);
        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId()))
                .thenReturn(Optional.empty());

        PlatformSyncResult result = worker.executeSync(account);

        assertEquals(SyncStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSnapshotCreated());
        verify(adapter, never()).fetchProfileData(anyString());
        verify(platformRateLimitManager, never()).tryAcquire(any());
    }

    @Test
    @DisplayName("executeSync — Rate limited request fails fast and sets FAILED status")
    void executeSync_RateLimited_FailsFast() {
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.LEETCODE)).thenReturn(adapter);
        when(platformRateLimitManager.tryAcquire(CodingPlatform.LEETCODE)).thenReturn(false);
        when(platformRateLimitManager.getEstimatedWaitTimeSeconds(CodingPlatform.LEETCODE)).thenReturn(15L);

        PlatformSyncResult result = worker.executeSync(account);

        assertEquals(SyncStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("Rate limit"));
        assertEquals(SyncStatus.FAILED, account.getSyncStatus());
        assertTrue(account.getLastSyncError().contains("Rate limit"));
        verify(adapter, never()).fetchProfileData(anyString());
    }
}
