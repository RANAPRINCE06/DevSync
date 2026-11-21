package com.devsync.coding.sync.service;

import com.devsync.coding.dto.PlatformAccountResponse;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.sync.dto.PlatformSyncResult;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for platform account synchronization management and scheduling coordination.
 */
public interface PlatformSyncService {

    /**
     * Synchronizes a single platform account with concurrency duplicate protection.
     *
     * @param account the account entity to synchronize
     * @return PlatformSyncResult indicating status, snapshot outcome, and errors
     */
    PlatformSyncResult syncAccount(PlatformAccount account);

    /**
     * Synchronizes all active platform accounts in the system, isolating individual platform failures.
     *
     * @return list of sync results for each active account
     */
    List<PlatformSyncResult> syncAllActiveAccounts();

    /**
     * Synchronizes a specific platform account on behalf of an authenticated user, enforcing ownership.
     *
     * @param accountId account identifier
     * @param userId authenticated user identifier
     * @return PlatformAccountResponse updated view
     */
    PlatformAccountResponse syncAccountForUser(UUID accountId, UUID userId);

    /**
     * Checks if a synchronization is currently in flight for the given account.
     *
     * @param accountId account identifier
     * @return true if currently running
     */
    boolean isAccountSyncInFlight(UUID accountId);
}
