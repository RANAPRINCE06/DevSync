package com.devsync.coding.analytics.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.coding.analytics.dto.*;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.common.exception.BadRequestException;
import com.devsync.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodingAnalyticsServiceTest — Unit Tests for Advanced Coding Analytics Calculations")
class CodingAnalyticsServiceTest {

    @Mock
    private PlatformAccountRepository platformAccountRepository;

    @Mock
    private PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CodingAnalyticsServiceImpl analyticsService;

    private UUID userId;
    private PlatformAccount lcAccount;
    private PlatformAccount cfAccount;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        lenient().when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        User user = new User();
        user.setId(userId);

        lcAccount = PlatformAccount.builder()
                .id(UUID.randomUUID())
                .user(user)
                .platform(CodingPlatform.LEETCODE)
                .username("test_lc")
                .profileUrl("https://leetcode.com/u/test_lc")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build();

        cfAccount = PlatformAccount.builder()
                .id(UUID.randomUUID())
                .user(user)
                .platform(CodingPlatform.CODEFORCES)
                .username("test_cf")
                .profileUrl("https://codeforces.com/profile/test_cf")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build();
    }

    @Test
    @DisplayName("getOverview — Returns aggregated metrics from latest snapshots")
    void getOverview_AggregatesFromLatestSnapshots() {
        when(platformAccountRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(lcAccount, cfAccount));

        PlatformStatSnapshot lcSnap = PlatformStatSnapshot.builder()
                .platformAccount(lcAccount)
                .problemsSolved(450)
                .currentRating(1850.0)
                .contestsParticipated(12)
                .globalRank(12000L)
                .snapshotAt(Instant.now())
                .build();

        PlatformStatSnapshot cfSnap = PlatformStatSnapshot.builder()
                .platformAccount(cfAccount)
                .problemsSolved(210)
                .currentRating(1620.0)
                .contestsParticipated(18)
                .snapshotAt(Instant.now())
                .build();

        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(lcAccount.getId()))
                .thenReturn(Optional.of(lcSnap));
        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(cfAccount.getId()))
                .thenReturn(Optional.of(cfSnap));
        when(platformStatSnapshotRepository.countByAccountIds(any())).thenReturn(15L);
        when(platformStatSnapshotRepository.findEarliestSnapshotAtByAccountIds(any()))
                .thenReturn(Instant.now().minus(30, ChronoUnit.DAYS));
        when(platformStatSnapshotRepository.findLatestSnapshotAtByAccountIds(any()))
                .thenReturn(Instant.now());

        CodingAnalyticsOverviewResponse overview = analyticsService.getOverview();

        assertEquals(660, overview.getTotalProblemsSolved(), "450 + 210 = 660");
        assertEquals(2, overview.getConnectedPlatformsCount());
        assertEquals(30, overview.getTotalContestsParticipated());
        assertEquals(1850.0, overview.getHighestRating());
        assertEquals(CodingPlatform.LEETCODE, overview.getHighestRatingPlatform());
        assertEquals(15L, overview.getTotalSnapshotsCollected());
        assertEquals(2, overview.getPlatformOverviews().size());
    }

    @Test
    @DisplayName("getOverview — Handles empty accounts gracefully")
    void getOverview_NoAccounts_ReturnsEmptyResponse() {
        when(platformAccountRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(userId))
                .thenReturn(Collections.emptyList());

        CodingAnalyticsOverviewResponse overview = analyticsService.getOverview();

        assertEquals(0, overview.getTotalProblemsSolved());
        assertEquals(0, overview.getConnectedPlatformsCount());
        assertNull(overview.getHighestRating());
        assertEquals(0L, overview.getTotalSnapshotsCollected());
        assertTrue(overview.getPlatformOverviews().isEmpty());
    }

    @Test
    @DisplayName("getRatingHistory — Calculates rating delta and personal best without cross-platform mixing")
    void getRatingHistory_CalculatesDeltaAndPB() {
        when(platformAccountRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(cfAccount));

        Instant t1 = Instant.now().minus(20, ChronoUnit.DAYS);
        Instant t2 = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant t3 = Instant.now();

        PlatformStatSnapshot s1 = PlatformStatSnapshot.builder()
                .platformAccount(cfAccount)
                .currentRating(1500.0)
                .snapshotAt(t1)
                .build();
        PlatformStatSnapshot s2 = PlatformStatSnapshot.builder()
                .platformAccount(cfAccount)
                .currentRating(1580.0)
                .snapshotAt(t2)
                .build();
        PlatformStatSnapshot s3 = PlatformStatSnapshot.builder()
                .platformAccount(cfAccount)
                .currentRating(1650.0)
                .snapshotAt(t3)
                .build();

        when(platformStatSnapshotRepository.findByAccountIdAndDateRangeAsc(eq(cfAccount.getId()), any(), any()))
                .thenReturn(List.of(s1, s2, s3));
        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(cfAccount.getId()))
                .thenReturn(Optional.of(s3));
        when(platformStatSnapshotRepository.findMaxCurrentRatingByAccountId(cfAccount.getId()))
                .thenReturn(1650.0);

        RatingHistoryResponse response = analyticsService.getRatingHistory(CodingPlatform.CODEFORCES, null, null, "30d");

        assertEquals(1, response.getPlatforms().size());
        RatingHistoryResponse.PlatformRatingSeries series = response.getPlatforms().getFirst();
        assertEquals(CodingPlatform.CODEFORCES, series.getPlatform());
        assertEquals(1650.0, series.getCurrentRating());
        assertEquals(1650.0, series.getPersonalBestRating());
        assertEquals(150.0, series.getRatingChange(), "1650 - 1500 = +150");
        assertEquals(3, series.getHistory().size());
    }

    @Test
    @DisplayName("getProblemDistribution — Categorizes difficulty and calculates historical period delta")
    void getProblemDistribution_ComputesTiersAndDelta() {
        when(platformAccountRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(lcAccount));

        PlatformStatSnapshot latest = PlatformStatSnapshot.builder()
                .platformAccount(lcAccount)
                .problemsSolved(500)
                .easySolved(200)
                .mediumSolved(220)
                .hardSolved(80)
                .snapshotAt(Instant.now())
                .build();

        PlatformStatSnapshot earliest = PlatformStatSnapshot.builder()
                .platformAccount(lcAccount)
                .problemsSolved(460)
                .easySolved(180)
                .mediumSolved(210)
                .hardSolved(70)
                .snapshotAt(Instant.now().minus(25, ChronoUnit.DAYS))
                .build();

        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(lcAccount.getId()))
                .thenReturn(Optional.of(latest));
        when(platformStatSnapshotRepository.findByAccountIdAndDateRangeAsc(eq(lcAccount.getId()), any(), any()))
                .thenReturn(List.of(earliest, latest));

        ProblemDistributionResponse response = analyticsService.getProblemDistribution(null, null, null, "30d");

        assertEquals(500, response.getTotalPlatformReportedSolved());
        assertEquals(200, response.getEasySolved());
        assertEquals(220, response.getMediumSolved());
        assertEquals(80, response.getHardSolved());
        assertEquals(0, response.getUnclassifiedSolved());
        assertEquals(40, response.getSolvedDeltaInPeriod(), "500 - 460 = 40 new problems");
        assertFalse(response.isVerifiedUniqueDeduplication());
        assertNotNull(response.getDeduplicationNote());
    }

    @Test
    @DisplayName("getActivity — Transparently reports aggregate snapshots and active calendar days")
    void getActivity_HonestDataModelReporting() {
        when(platformAccountRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(lcAccount));

        Instant t1 = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant t2 = Instant.now().minus(1, ChronoUnit.DAYS);

        PlatformStatSnapshot s1 = PlatformStatSnapshot.builder()
                .platformAccount(lcAccount)
                .problemsSolved(300)
                .currentRating(1700.0)
                .snapshotAt(t1)
                .build();

        PlatformStatSnapshot s2 = PlatformStatSnapshot.builder()
                .platformAccount(lcAccount)
                .problemsSolved(305)
                .currentRating(1720.0)
                .snapshotAt(t2)
                .build();

        when(platformStatSnapshotRepository.findByAccountIdsAndDateRangeAsc(any(), any(), any()))
                .thenReturn(List.of(s1, s2));

        CodingActivityResponse activity = analyticsService.getActivity(null, null, "7d");

        assertFalse(activity.isDailySubmissionActivitySupported(),
                "Must honestly indicate daily submission heatmaps are not supported by aggregate snapshots");
        assertNotNull(activity.getCoverageNotice());
        assertEquals(2, activity.getTotalSnapshotsInPeriod());
        assertEquals(2, activity.getActiveDaysWithSnapshots());
        assertEquals(2, activity.getSnapshotTimeline().size());
        assertEquals(5, activity.getSnapshotTimeline().get(1).getSolvedChange(), "305 - 300 = +5");
        assertEquals(20.0, activity.getSnapshotTimeline().get(1).getRatingChange(), "1720 - 1700 = +20");
    }

    @Test
    @DisplayName("getContestPerformance — Excludes platforms without contest systems")
    void getContestPerformance_OnlyIncludesContestPlatforms() {
        PlatformAccount ghAccount = PlatformAccount.builder()
                .id(UUID.randomUUID())
                .platform(CodingPlatform.GITHUB)
                .username("gh_user")
                .active(true)
                .build();

        when(platformAccountRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(cfAccount, ghAccount));

        PlatformStatSnapshot cfSnap = PlatformStatSnapshot.builder()
                .platformAccount(cfAccount)
                .contestsParticipated(25)
                .currentRating(1680.0)
                .snapshotAt(Instant.now())
                .build();

        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(cfAccount.getId()))
                .thenReturn(Optional.of(cfSnap));
        when(platformStatSnapshotRepository.findFirstByPlatformAccountIdOrderBySnapshotAtDesc(ghAccount.getId()))
                .thenReturn(Optional.empty());
        when(platformStatSnapshotRepository.findByAccountIdAndDateRangeAsc(eq(cfAccount.getId()), any(), any()))
                .thenReturn(List.of(cfSnap));

        ContestPerformanceResponse response = analyticsService.getContestPerformance(null, null, null, "all");

        assertEquals(25, response.getTotalContestsParticipated());
        assertTrue(response.isAnyContestsSupported());
        assertEquals(2, response.getPlatformContests().size());

        ContestPerformanceResponse.PlatformContestRecord cfRecord = response.getPlatformContests().stream()
                .filter(r -> r.getPlatform() == CodingPlatform.CODEFORCES).findFirst().orElseThrow();
        assertTrue(cfRecord.isContestsSupported());
        assertEquals(25, cfRecord.getContestsParticipated());

        ContestPerformanceResponse.PlatformContestRecord ghRecord = response.getPlatformContests().stream()
                .filter(r -> r.getPlatform() == CodingPlatform.GITHUB).findFirst().orElseThrow();
        assertFalse(ghRecord.isContestsSupported());
    }

    @Test
    @DisplayName("getInsights — Produces factual data-backed insights")
    void getInsights_GeneratesDataBackedInsights() {
        when(platformAccountRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(lcAccount));

        PlatformStatSnapshot first = PlatformStatSnapshot.builder()
                .platformAccount(lcAccount)
                .currentRating(1700.0)
                .problemsSolved(400)
                .snapshotAt(Instant.now().minus(20, ChronoUnit.DAYS))
                .build();

        PlatformStatSnapshot last = PlatformStatSnapshot.builder()
                .platformAccount(lcAccount)
                .currentRating(1780.0)
                .problemsSolved(430)
                .snapshotAt(Instant.now())
                .build();

        when(platformStatSnapshotRepository.findByAccountIdAndDateRangeAsc(eq(lcAccount.getId()), any(), any()))
                .thenReturn(List.of(first, last));
        when(platformStatSnapshotRepository.findMaxCurrentRatingByAccountId(lcAccount.getId()))
                .thenReturn(1780.0);

        PerformanceInsightsResponse response = analyticsService.getInsights(null, null, null, "30d");

        assertFalse(response.getInsights().isEmpty());
        assertTrue(response.getInsights().stream().anyMatch(i -> i.getCategory().equals("RATING") && i.getMessage().contains("climbed by +80.0")));
        assertTrue(response.getInsights().stream().anyMatch(i -> i.getTitle().equals("Personal Best")));
        assertTrue(response.getInsights().stream().anyMatch(i -> i.getCategory().equals("SOLVED") && i.getMessage().contains("Solved 30 new problem(s)")));
    }

    @Test
    @DisplayName("Date Range Validation — Rejects 'from' after 'to' or invalid period string")
    void dateRange_InvalidInputs_ThrowsBadRequestException() {
        Instant from = Instant.now();
        Instant to = from.minus(5, ChronoUnit.DAYS);

        assertThrows(BadRequestException.class, () ->
                analyticsService.getRatingHistory(null, from, to, null));

        assertThrows(BadRequestException.class, () ->
                analyticsService.getRatingHistory(null, null, null, "999years"));
    }
}
