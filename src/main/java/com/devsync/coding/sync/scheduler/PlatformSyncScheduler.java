package com.devsync.coding.sync.scheduler;

import com.devsync.coding.sync.config.CodingSyncProperties;
import com.devsync.coding.sync.service.PlatformSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Periodically triggers automated synchronization of active coding platform accounts.
 * Prevents overlapping scheduled runs using an atomic execution lock and respects
 * centralized configuration properties.
 */
@Component
public class PlatformSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(PlatformSyncScheduler.class);

    private final PlatformSyncService platformSyncService;
    private final CodingSyncProperties codingSyncProperties;
    private final AtomicBoolean isJobRunning = new AtomicBoolean(false);

    public PlatformSyncScheduler(PlatformSyncService platformSyncService,
                                 CodingSyncProperties codingSyncProperties) {
        this.platformSyncService = platformSyncService;
        this.codingSyncProperties = codingSyncProperties;
    }

    /**
     * Periodically triggers batch synchronization for all active platform accounts.
     * Fixed delay is measured from the completion of the previous run.
     */
    @Scheduled(
            fixedDelayString = "${app.coding.sync.fixed-delay-ms:21600000}",
            initialDelayString = "${app.coding.sync.initial-delay-ms:60000}"
    )
    public void runScheduledSync() {
        if (!codingSyncProperties.isEnabled()) {
            log.debug("Automated coding platform synchronization is disabled (app.coding.sync.enabled=false)");
            return;
        }

        if (!isJobRunning.compareAndSet(false, true)) {
            log.warn("Previous automated platform sync is still executing; skipping overlapping cycle");
            return;
        }

        log.info("Starting scheduled platform synchronization cycle");
        try {
            platformSyncService.syncAllActiveAccounts();
        } catch (Exception ex) {
            log.error("Unexpected failure during scheduled platform synchronization cycle: {}", ex.getMessage(), ex);
        } finally {
            isJobRunning.set(false);
            log.info("Completed scheduled platform synchronization cycle");
        }
    }

    public boolean isJobRunning() {
        return isJobRunning.get();
    }
}
