package com.devsync.coding.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.coding.dto.ConnectPlatformRequest;
import com.devsync.coding.dto.PlatformAccountResponse;
import com.devsync.coding.dto.PlatformStatSnapshotResponse;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.registry.PlatformIntegrationRegistry;
import com.devsync.coding.mapper.CodingProfileMapper;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.coding.sync.service.PlatformSyncService;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("codingIntelligenceProfileService")
@Transactional(readOnly = true)
public class CodingProfileServiceImpl implements CodingProfileService {

    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository platformStatSnapshotRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PlatformSyncService platformSyncService;

    public CodingProfileServiceImpl(PlatformAccountRepository platformAccountRepository,
                                    PlatformStatSnapshotRepository platformStatSnapshotRepository,
                                    UserRepository userRepository,
                                    CurrentUserProvider currentUserProvider,
                                    PlatformSyncService platformSyncService) {
        this.platformAccountRepository = platformAccountRepository;
        this.platformStatSnapshotRepository = platformStatSnapshotRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.platformSyncService = platformSyncService;
    }

    @Override
    @Transactional
    public PlatformAccountResponse connectPlatform(ConnectPlatformRequest request) {
        if (request.getPlatform() == null) {
            throw new BadRequestException("Platform is required");
        }
        if (!CodingPlatform.isValidUsername(request.getUsername())) {
            throw new BadRequestException("Invalid username format. Must be alphanumeric and up to 100 characters.");
        }

        UUID currentUserId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (platformAccountRepository.existsByUserIdAndPlatformAndActiveTrue(currentUserId, request.getPlatform())) {
            throw new BadRequestException("An active account for platform " + request.getPlatform() + " already exists for this user.");
        }

        String profileUrl = request.getPlatform().buildProfileUrl(request.getUsername().trim());

        PlatformAccount account = PlatformAccount.builder()
                .user(user)
                .platform(request.getPlatform())
                .username(request.getUsername().trim())
                .profileUrl(profileUrl)
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build();

        PlatformAccount saved = platformAccountRepository.save(account);
        return CodingProfileMapper.toResponse(saved, null);
    }

    @Override
    public List<PlatformAccountResponse> getConnectedAccounts() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> accounts = platformAccountRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(currentUserId);

        return accounts.stream()
                .map(account -> {
                    Optional<PlatformStatSnapshot> latest = platformStatSnapshotRepository
                            .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());
                    return CodingProfileMapper.toResponse(account, latest.orElse(null));
                })
                .toList();
    }

    @Override
    public PlatformAccountResponse getAccountById(UUID accountId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        PlatformAccount account = platformAccountRepository.findByIdAndUserIdAndActiveTrue(accountId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found: " + accountId));

        PlatformStatSnapshot latest = platformStatSnapshotRepository
                .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId())
                .orElse(null);

        return CodingProfileMapper.toResponse(account, latest);
    }

    @Override
    @Transactional
    public void disconnectAccount(UUID accountId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        PlatformAccount account = platformAccountRepository.findByIdAndUserIdAndActiveTrue(accountId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found: " + accountId));

        account.setActive(false);
        platformAccountRepository.save(account);
    }

    @Override
    @Transactional
    public PlatformAccountResponse syncAccount(UUID accountId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return platformSyncService.syncAccountForUser(accountId, currentUserId);
    }

    @Override
    public Page<PlatformStatSnapshotResponse> getAccountHistory(UUID accountId, Pageable pageable) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        // IDOR protection: ensure account belongs to authenticated user
        platformAccountRepository.findByIdAndUserId(accountId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found: " + accountId));

        Page<PlatformStatSnapshot> historyPage = platformStatSnapshotRepository
                .findAllByPlatformAccountIdOrderBySnapshotAtDesc(accountId, pageable);

        return historyPage.map(CodingProfileMapper::toSnapshotResponse);
    }
}
