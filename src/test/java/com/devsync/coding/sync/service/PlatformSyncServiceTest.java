package com.devsync.coding.sync.service;

import com.devsync.coding.dto.PlatformAccountResponse;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.reliability.cache.PlatformProfileCache;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.coding.sync.dto.PlatformSyncResult;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformSyncServiceTest — Unit Tests for Coordination & In-Flight Concurrency")
class PlatformSyncServiceTest {

    @Mock
    private PlatformSyncWorker platformSyncWorker;

    @Mock
    private PlatformAccountRepository platformAccountRepository;

    @Mock
    private PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @Mock
    private PlatformProfileCache platformProfileCache;

    @InjectMocks
    private PlatformSyncServiceImpl platformSyncService;

    private User alice;
    private PlatformAccount cfAccount;
    private PlatformAccount lcAccount;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setId(UUID.randomUUID());

        cfAccount = PlatformAccount.builder()
                .id(UUID.randomUUID())
                .user(alice)
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .profileUrl("https://codeforces.com/profile/alice_cf")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build();

        lcAccount = PlatformAccount.builder()
                .id(UUID.randomUUID())
                .user(alice)
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/u/alice_lc")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build();
    }

    @Test
    @DisplayName("syncAllActiveAccounts — Synchronizes all active platform accounts")
    void syncAllActiveAccounts_MultipleAccounts_ExecutesAll() {
        when(platformAccountRepository.findAllByActiveTrue()).thenReturn(List.of(cfAccount, lcAccount));

        when(platformSyncWorker.executeSync(cfAccount)).thenReturn(PlatformSyncResult.builder()
                .accountId(cfAccount.getId())
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .status(SyncStatus.SUCCESS)
                .snapshotCreated(true)
                .timestamp(Instant.now())
                .build());

        when(platformSyncWorker.executeSync(lcAccount)).thenReturn(PlatformSyncResult.builder()
                .accountId(lcAccount.getId())
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .status(SyncStatus.SUCCESS)
                .snapshotCreated(true)
                .timestamp(Instant.now())
                .build());

        List<PlatformSyncResult> results = platformSyncService.syncAllActiveAccounts();

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getStatus() == SyncStatus.SUCCESS));
        verify(platformSyncWorker).executeSync(cfAccount);
        verify(platformSyncWorker).executeSync(lcAccount);
    }

    @Test
    @DisplayName("syncAllActiveAccounts — One platform failure is isolated and does not stop others")
    void syncAllActiveAccounts_SinglePlatformFailure_DoesNotHaltOtherPlatforms() {
        when(platformAccountRepository.findAllByActiveTrue()).thenReturn(List.of(cfAccount, lcAccount));

        when(platformSyncWorker.executeSync(cfAccount)).thenReturn(PlatformSyncResult.builder()
                .accountId(cfAccount.getId())
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .status(SyncStatus.FAILED)
                .errorMessage("Codeforces API timeout")
                .timestamp(Instant.now())
                .build());

        when(platformSyncWorker.executeSync(lcAccount)).thenReturn(PlatformSyncResult.builder()
                .accountId(lcAccount.getId())
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .status(SyncStatus.SUCCESS)
                .snapshotCreated(true)
                .timestamp(Instant.now())
                .build());

        List<PlatformSyncResult> results = platformSyncService.syncAllActiveAccounts();

        assertEquals(2, results.size());
        assertEquals(SyncStatus.FAILED, results.get(0).getStatus());
        assertEquals(SyncStatus.SUCCESS, results.get(1).getStatus());

        verify(platformSyncWorker).executeSync(cfAccount);
        verify(platformSyncWorker).executeSync(lcAccount);
    }

    @Test
    @DisplayName("syncAccount — In-flight account is not synchronized concurrently")
    void syncAccount_ConcurrentDuplicate_InFlightSkipped() {
        // Mock worker execution to simulate slow background sync
        when(platformSyncWorker.executeSync(cfAccount)).thenAnswer(invocation -> {
            // While executing, second call should report in-flight
            assertTrue(platformSyncService.isAccountSyncInFlight(cfAccount.getId()));
            PlatformSyncResult duplicateAttempt = platformSyncService.syncAccount(cfAccount);
            assertTrue(duplicateAttempt.isInFlightSkipped());
            return PlatformSyncResult.builder()
                    .accountId(cfAccount.getId())
                    .platform(CodingPlatform.CODEFORCES)
                    .username("alice_cf")
                    .status(SyncStatus.SUCCESS)
                    .timestamp(Instant.now())
                    .build();
        });

        PlatformSyncResult primaryResult = platformSyncService.syncAccount(cfAccount);
        assertEquals(SyncStatus.SUCCESS, primaryResult.getStatus());
        assertFalse(primaryResult.isInFlightSkipped());

        // Lock released after execution completes
        assertFalse(platformSyncService.isAccountSyncInFlight(cfAccount.getId()));
    }

    @Test
    @DisplayName("syncAccountForUser — Enforces IDOR ownership protection (throws 404 for foreign account)")
    void syncAccountForUser_CrossUser_ThrowsResourceNotFoundException() {
        UUID foreignUserId = UUID.randomUUID();
        when(platformAccountRepository.findByIdAndUserIdAndActiveTrue(cfAccount.getId(), foreignUserId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                platformSyncService.syncAccountForUser(cfAccount.getId(), foreignUserId));

        verify(platformSyncWorker, never()).executeSync(any());
    }

    @Test
    @DisplayName("syncAccountForUser — Valid user sync returns updated account response")
    void syncAccountForUser_ValidOwner_ReturnsResponse() {
        when(platformAccountRepository.findByIdAndUserIdAndActiveTrue(cfAccount.getId(), alice.getId()))
                .thenReturn(Optional.of(cfAccount));
        when(platformAccountRepository.findById(cfAccount.getId())).thenReturn(Optional.of(cfAccount));
        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(cfAccount.getId()))
                .thenReturn(Optional.empty());
        when(platformSyncWorker.executeSync(cfAccount)).thenReturn(PlatformSyncResult.builder()
                .accountId(cfAccount.getId())
                .status(SyncStatus.SUCCESS)
                .build());

        PlatformAccountResponse response = platformSyncService.syncAccountForUser(cfAccount.getId(), alice.getId());
        assertNotNull(response);
        assertEquals(cfAccount.getId(), response.getId());
    }
}
