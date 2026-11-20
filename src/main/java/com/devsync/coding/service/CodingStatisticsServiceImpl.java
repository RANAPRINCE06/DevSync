package com.devsync.coding.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.coding.dto.CodingStatisticsResponse;
import com.devsync.coding.dto.PlatformStatSnapshotResponse;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.mapper.CodingProfileMapper;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class CodingStatisticsServiceImpl implements CodingStatisticsService {

    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository platformStatSnapshotRepository;
    private final CurrentUserProvider currentUserProvider;

    public CodingStatisticsServiceImpl(PlatformAccountRepository platformAccountRepository,
                                       PlatformStatSnapshotRepository platformStatSnapshotRepository,
                                       CurrentUserProvider currentUserProvider) {
        this.platformAccountRepository = platformAccountRepository;
        this.platformStatSnapshotRepository = platformStatSnapshotRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public CodingStatisticsResponse getAggregatedStatistics() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> activeAccounts = platformAccountRepository
                .findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(currentUserId);

        if (activeAccounts.isEmpty()) {
            return new CodingStatisticsResponse(null, null, null, null, 0, Collections.emptyList());
        }

        List<PlatformStatSnapshotResponse> latestSnapshots = new ArrayList<>();
        int problemsSolvedTotal = 0;
        boolean hasProblemsSolved = false;

        int contestsParticipatedTotal = 0;
        boolean hasContests = false;

        Double highestRating = null;
        CodingPlatform highestRatingPlatform = null;

        for (PlatformAccount account : activeAccounts) {
            Optional<PlatformStatSnapshot> snapshotOpt = platformStatSnapshotRepository
                    .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

            if (snapshotOpt.isPresent()) {
                PlatformStatSnapshot snapshot = snapshotOpt.get();
                latestSnapshots.add(CodingProfileMapper.toSnapshotResponse(snapshot));

                if (snapshot.getProblemsSolved() != null) {
                    problemsSolvedTotal += snapshot.getProblemsSolved();
                    hasProblemsSolved = true;
                }

                if (snapshot.getContestsParticipated() != null) {
                    contestsParticipatedTotal += snapshot.getContestsParticipated();
                    hasContests = true;
                }

                Double accountRating = snapshot.getCurrentRating() != null
                        ? snapshot.getCurrentRating()
                        : snapshot.getMaximumRating();

                if (accountRating != null) {
                    if (highestRating == null || accountRating > highestRating) {
                        highestRating = accountRating;
                        highestRatingPlatform = account.getPlatform();
                    }
                }
            }
        }

        return new CodingStatisticsResponse(
                hasProblemsSolved ? problemsSolvedTotal : null,
                hasContests ? contestsParticipatedTotal : null,
                highestRating,
                highestRatingPlatform,
                activeAccounts.size(),
                latestSnapshots
        );
    }
}
