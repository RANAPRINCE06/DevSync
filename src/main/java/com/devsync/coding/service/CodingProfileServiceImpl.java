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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service("codingIntelligenceProfileService")
@Transactional(readOnly = true)
public class CodingProfileServiceImpl implements CodingProfileService {

    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository platformStatSnapshotRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PlatformIntegrationRegistry platformIntegrationRegistry;

    public CodingProfileServiceImpl(PlatformAccountRepository platformAccountRepository,
                                    PlatformStatSnapshotRepository platformStatSnapshotRepository,
                                    UserRepository userRepository,
                                    CurrentUserProvider currentUserProvider,
                                    PlatformIntegrationRegistry platformIntegrationRegistry) {
        this.platformAccountRepository = platformAccountRepository;
        this.platformStatSnapshotRepository = platformStatSnapshotRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.platformIntegrationRegistry = platformIntegrationRegistry;
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
        PlatformAccount account = platformAccountRepository.findByIdAndUserIdAndActiveTrue(accountId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found: " + accountId));

        CodingPlatformAdapter adapter = platformIntegrationRegistry.getRequiredAdapter(account.getPlatform());
        account.setLastSyncAttemptAt(Instant.now());

        try {
            ExternalProfileData data = adapter.fetchProfileData(account.getUsername());
            if (data == null) {
                throw new PlatformIntegrationException("External platform adapter returned empty data");
            }

            // Check if statistics are identical to the latest snapshot (avoid duplicate snapshot creation)
            Optional<PlatformStatSnapshot> latestOpt = platformStatSnapshotRepository
                    .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

            PlatformStatSnapshot snapshotToReturn;
            if (latestOpt.isPresent() && isSameStatistics(latestOpt.get(), data)) {
                snapshotToReturn = latestOpt.get();
            } else {
                PlatformStatSnapshot newSnapshot = PlatformStatSnapshot.builder()
                        .platformAccount(account)
                        .currentRating(data.getCurrentRating())
                        .maximumRating(data.getMaximumRating())
                        .globalRank(data.getGlobalRank())
                        .countryRank(data.getCountryRank())
                        .problemsSolved(data.getProblemsSolved())
                        .easySolved(data.getEasySolved())
                        .mediumSolved(data.getMediumSolved())
                        .hardSolved(data.getHardSolved())
                        .contestsParticipated(data.getContestsParticipated())
                        .additionalStats(data.getAdditionalStats())
                        .snapshotAt(data.getFetchedAt() != null ? data.getFetchedAt() : Instant.now())
                        .build();
                snapshotToReturn = platformStatSnapshotRepository.save(newSnapshot);
            }

            account.setSyncStatus(SyncStatus.SUCCESS);
            account.setLastSyncedAt(Instant.now());
            account.setLastSyncError(null);
            PlatformAccount savedAccount = platformAccountRepository.save(account);

            return CodingProfileMapper.toResponse(savedAccount, snapshotToReturn);

        } catch (Exception ex) {
            account.setSyncStatus(SyncStatus.FAILED);
            String sanitizedError = sanitizeErrorMessage(ex.getMessage());
            account.setLastSyncError(sanitizedError);
            PlatformAccount savedAccount = platformAccountRepository.save(account);

            // Preserve the previous successful snapshot on failure
            PlatformStatSnapshot latestExisting = platformStatSnapshotRepository
                    .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId())
                    .orElse(null);

            return CodingProfileMapper.toResponse(savedAccount, latestExisting);
        }
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

    private boolean isSameMap(java.util.Map<String, Object> m1, java.util.Map<String, Object> m2) {
        boolean m1Empty = (m1 == null || m1.isEmpty());
        boolean m2Empty = (m2 == null || m2.isEmpty());
        if (m1Empty && m2Empty) {
            return true;
        }
        return Objects.equals(m1, m2);
    }

    private String sanitizeErrorMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Unknown platform synchronization error";
        }
        String sanitized = message.replaceAll("(?i)(password|secret|token|key|authorization)=[^\s&]+", "$1=***");
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 497) + "...";
        }
        return sanitized;
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
