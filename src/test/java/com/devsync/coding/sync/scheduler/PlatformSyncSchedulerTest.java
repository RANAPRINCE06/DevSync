package com.devsync.coding.sync.scheduler;

import com.devsync.coding.sync.config.CodingSyncProperties;
import com.devsync.coding.sync.dto.PlatformSyncResult;
import com.devsync.coding.sync.service.PlatformSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformSyncSchedulerTest — Automated Scheduler Trigger & Overlap Protection")
class PlatformSyncSchedulerTest {

    @Mock
    private PlatformSyncService platformSyncService;

    @Mock
    private CodingSyncProperties codingSyncProperties;

    @InjectMocks
    private PlatformSyncScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(codingSyncProperties.isEnabled()).thenReturn(true);
    }

    @Test
    @DisplayName("runScheduledSync — When enabled, triggers batch synchronization of active accounts")
    void runScheduledSync_WhenEnabled_TriggersBatchSync() {
        when(platformSyncService.syncAllActiveAccounts()).thenReturn(List.of());

        scheduler.runScheduledSync();

        verify(platformSyncService).syncAllActiveAccounts();
        assertFalse(scheduler.isJobRunning(), "Lock must be released after completion");
    }

    @Test
    @DisplayName("runScheduledSync — When disabled via configuration, skips execution entirely")
    void runScheduledSync_WhenDisabled_SkipsExecution() {
        when(codingSyncProperties.isEnabled()).thenReturn(false);

        scheduler.runScheduledSync();

        verifyNoInteractions(platformSyncService);
        assertFalse(scheduler.isJobRunning());
    }

    @Test
    @DisplayName("runScheduledSync — Prevents overlapping executions when a job is already in progress")
    void runScheduledSync_WhenAlreadyRunning_PreventsOverlappingCycle() {
        when(platformSyncService.syncAllActiveAccounts()).thenAnswer(invocation -> {
            // While the first job is running, simulate a concurrent scheduler trigger
            assertTrue(scheduler.isJobRunning(), "Job running lock must be active");
            scheduler.runScheduledSync(); // Re-entrant or concurrent trigger attempt
            return List.of();
        });

        scheduler.runScheduledSync();

        // Exactly 1 invocation of syncAllActiveAccounts should have occurred
        verify(platformSyncService, times(1)).syncAllActiveAccounts();
        assertFalse(scheduler.isJobRunning(), "Job running lock must be released");
    }

    @Test
    @DisplayName("runScheduledSync — Uncaught exception is caught and lock is safely released")
    void runScheduledSync_HandlesExceptionGracefully() {
        when(platformSyncService.syncAllActiveAccounts())
                .thenThrow(new RuntimeException("Database connectivity lost"));

        scheduler.runScheduledSync();

        assertFalse(scheduler.isJobRunning(), "Lock must always be released in finally block");
    }
}
