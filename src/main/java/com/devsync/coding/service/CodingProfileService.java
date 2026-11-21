package com.devsync.coding.service;

import com.devsync.coding.dto.ConnectPlatformRequest;
import com.devsync.coding.dto.PlatformAccountResponse;
import com.devsync.coding.dto.PlatformStatSnapshotResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CodingProfileService {

    PlatformAccountResponse connectPlatform(ConnectPlatformRequest request);

    List<PlatformAccountResponse> getConnectedAccounts();

    PlatformAccountResponse getAccountById(UUID accountId);

    void disconnectAccount(UUID accountId);

    PlatformAccountResponse syncAccount(UUID accountId);

    Page<PlatformStatSnapshotResponse> getAccountHistory(UUID accountId, Pageable pageable);
}
