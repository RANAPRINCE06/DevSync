package com.devsync.coding.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.coding.dto.ConnectPlatformRequest;
import com.devsync.coding.dto.PlatformAccountResponse;
import com.devsync.coding.dto.PlatformStatSnapshotResponse;
import com.devsync.coding.dto.ProfilePreviewResponse;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformRateLimitException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import com.devsync.coding.integration.registry.PlatformIntegrationRegistry;
import com.devsync.coding.mapper.CodingProfileMapper;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.coding.service.url.PlatformUrlDetector;
import com.devsync.coding.sync.service.PlatformSyncService;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service("codingIntelligenceProfileService")
@Transactional(readOnly = true)
public class CodingProfileServiceImpl implements CodingProfileService {

    private static final Logger log = LoggerFactory.getLogger(CodingProfileServiceImpl.class);

    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository platformStatSnapshotRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PlatformSyncService platformSyncService;
    private final PlatformIntegrationRegistry platformIntegrationRegistry;

    public CodingProfileServiceImpl(PlatformAccountRepository platformAccountRepository,
            PlatformStatSnapshotRepository platformStatSnapshotRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            PlatformSyncService platformSyncService,
            PlatformIntegrationRegistry platformIntegrationRegistry) {
        this.platformAccountRepository = platformAccountRepository;
        this.platformStatSnapshotRepository = platformStatSnapshotRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.platformSyncService = platformSyncService;
        this.platformIntegrationRegistry = platformIntegrationRegistry;
    }

    @Override
    public ProfilePreviewResponse previewProfile(String rawUrl) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        // 1. Detect platform and username from raw URL (or throw BadRequestException)
        PlatformUrlDetector.DetectedProfileUrl detected = PlatformUrlDetector.detect(rawUrl);
        CodingPlatform platform = detected.platform();
        String username = detected.username();
        String canonicalUrl = detected.canonicalProfileUrl();

        // 2. Check if user already has an active account on this platform
        boolean alreadyConnected = platformAccountRepository.existsByUserIdAndPlatformAndActiveTrue(currentUserId,
                platform);

        // 3. Platform capability information
        boolean autoSync = platform.isAutomatedSyncSupported();
        String capabilityNotice = null;
        if (platform == CodingPlatform.ATCODER) {
            capabilityNotice = "Statistics provided by community Kenkoooo API. Contest rating not tracked.";
        } else if (platform == CodingPlatform.GEEKSFORGEEKS) {
            capabilityNotice = "Statistics provided by community GeeksforGeeks API. Contest rating not tracked.";
        } else if (platform == CodingPlatform.CODECHEF) {
            capabilityNotice = "Official automated synchronization requires OAuth2 credentials. Profile linking is supported.";
        } else if (platform == CodingPlatform.HACKERRANK) {
            capabilityNotice = "Automated synchronization is restricted to Enterprise assessment APIs (HackerRank for Work). Profile linking is supported.";
        }

        // 4. Fetch live profile data if platform supports automated sync (without
        // persisting anything)
        boolean profileExists = true;
        String warning = null;
        String displayName = null;
        String avatarUrl = null;
        ProfilePreviewResponse.PreviewMetrics metrics = null;

        if (autoSync) {
            try {
                CodingPlatformAdapter adapter = platformIntegrationRegistry.getRequiredAdapter(platform);
                ExternalProfileData data = adapter.fetchProfileData(username);
                if (data != null) {
                    Map<String, Object> additionalStats = data.getAdditionalStats();
                    if (additionalStats != null) {
                        Object dn = additionalStats.get("displayName");
                        if (dn instanceof String s && !s.isBlank()) {
                            displayName = s;
                        }
                        Object av = additionalStats.get("avatarUrl");
                        if (av instanceof String s && !s.isBlank()) {
                            avatarUrl = s;
                        }
                    }

                    Integer publicRepos = null;
                    Integer totalStars = null;
                    Integer followers = null;
                    if (platform == CodingPlatform.GITHUB && additionalStats != null) {
                        Object r = additionalStats.get("publicRepos");
                        if (r instanceof Number n)
                            publicRepos = n.intValue();
                        Object s = additionalStats.get("totalStars");
                        if (s instanceof Number n)
                            totalStars = n.intValue();
                        Object f = additionalStats.get("followers");
                        if (f instanceof Number n)
                            followers = n.intValue();
                    }

                    metrics = new ProfilePreviewResponse.PreviewMetrics(
                            data.getProblemsSolved(),
                            data.getEasySolved(),
                            data.getMediumSolved(),
                            data.getHardSolved(),
                            data.getCurrentRating(),
                            data.getMaximumRating(),
                            data.getGlobalRank(),
                            data.getPlatformRank(),
                            data.getContestsParticipated(),
                            publicRepos,
                            totalStars,
                            followers,
                            additionalStats);
                }
            } catch (PlatformUserNotFoundException ex) {
                profileExists = false;
                warning = "Profile '@" + username + "' was not found on " + platform.getDisplayName()
                        + ". Please check handle spelling.";
            } catch (PlatformRateLimitException ex) {
                warning = "External " + platform.getDisplayName()
                        + " rate limit reached. Preview statistics temporarily unavailable.";
            } catch (PlatformIntegrationException ex) {
                warning = "Could not fetch preview statistics from " + platform.getDisplayName() + " ("
                        + ex.getMessage() + "). Profile link can still be connected.";
            } catch (Exception ex) {
                log.warn("Unexpected error previewing profile for {}@{}: {}", platform, username, ex.getMessage());
                warning = "Could not reach " + platform.getDisplayName() + ". Profile link can still be connected.";
            }
        }

        return new ProfilePreviewResponse(
                platform,
                platform.getDisplayName(),
                username,
                canonicalUrl,
                profileExists,
                displayName,
                avatarUrl,
                autoSync,
                capabilityNotice,
                warning,
                alreadyConnected,
                metrics);
    }

    @Override
    @Transactional
    public PlatformAccountResponse connectPlatform(ConnectPlatformRequest request) {
        CodingPlatform platform = request.getPlatform();
        String username = request.getUsername();
        String profileUrl = request.getProfileUrl();

        // Intelligent auto-detection from URL if platform or username omitted
        if ((platform == null || username == null || username.trim().isEmpty())
                && (profileUrl != null && !profileUrl.trim().isEmpty())) {
            PlatformUrlDetector.DetectedProfileUrl detected = PlatformUrlDetector.detect(profileUrl);
            platform = detected.platform();
            username = detected.username();
            profileUrl = detected.canonicalProfileUrl();
        }

        if (platform == null) {
            throw new BadRequestException("Platform is required");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        if (!CodingPlatform.isValidUsername(username)) {
            throw new BadRequestException("Invalid username format. Must be alphanumeric and up to 100 characters.");
        }

        UUID currentUserId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (platformAccountRepository.existsByUserIdAndPlatformAndActiveTrue(currentUserId, platform)) {
            throw new BadRequestException(
                    "An active account for platform " + platform + " already exists for this user.");
        }

        if (profileUrl == null || profileUrl.trim().isEmpty()) {
            profileUrl = platform.buildProfileUrl(username.trim());
        }

        PlatformAccount account = PlatformAccount.builder()
                .user(user)
                .platform(platform)
                .username(username.trim())
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
        List<PlatformAccount> accounts = platformAccountRepository
                .findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(currentUserId);

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
        platformSyncService.evictAccountCache(account.getPlatform(), account.getUsername());
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
