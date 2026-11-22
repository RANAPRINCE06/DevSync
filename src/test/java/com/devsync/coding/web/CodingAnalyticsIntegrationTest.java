package com.devsync.coding.web;

import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CodingAnalyticsIntegrationTest — REST Endpoints & Multi-Tenant IDOR Protection")
class CodingAnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformAccountRepository platformAccountRepository;

    @Autowired
    private PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User alice;
    private User bob;
    private String aliceToken;
    private String bobToken;

    @BeforeEach
    void setUp() {
        platformStatSnapshotRepository.deleteAll();
        platformAccountRepository.deleteAll();
        userRepository.deleteAll();

        alice = userRepository.save(new User(
                null, "Alice Analytics", "alice.analytics@devsync.com",
                passwordEncoder.encode("Password123!"), null, "UTC", true,
                Instant.now(), Instant.now()
        ));

        bob = userRepository.save(new User(
                null, "Bob Analytics", "bob.analytics@devsync.com",
                passwordEncoder.encode("Password123!"), null, "UTC", true,
                Instant.now(), Instant.now()
        ));

        aliceToken = jwtTokenProvider.generateToken(alice.getId(), alice.getEmail(), List.of("ROLE_USER"));
        bobToken = jwtTokenProvider.generateToken(bob.getId(), bob.getEmail(), List.of("ROLE_USER"));

        // Seed Alice with accounts and snapshots
        PlatformAccount aliceLc = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/u/alice_lc")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        PlatformAccount aliceCf = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .profileUrl("https://codeforces.com/profile/alice_cf")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        // Snapshot 1 (earlier)
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(aliceLc)
                .problemsSolved(400)
                .easySolved(150)
                .mediumSolved(200)
                .hardSolved(50)
                .currentRating(1750.0)
                .maximumRating(1750.0)
                .contestsParticipated(8)
                .snapshotAt(Instant.now().minus(20, ChronoUnit.DAYS))
                .build());

        // Snapshot 2 (latest)
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(aliceLc)
                .problemsSolved(440)
                .easySolved(160)
                .mediumSolved(220)
                .hardSolved(60)
                .currentRating(1820.0)
                .maximumRating(1820.0)
                .contestsParticipated(10)
                .snapshotAt(Instant.now())
                .build());

        // Codeforces snapshot
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(aliceCf)
                .problemsSolved(180)
                .currentRating(1600.0)
                .maximumRating(1650.0)
                .contestsParticipated(15)
                .snapshotAt(Instant.now())
                .build());
    }

    @Test
    @DisplayName("GET /overview — Alice retrieves aggregated statistics from her active accounts")
    void getOverview_Alice_ReturnsAggregatedMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/overview")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalProblemsSolved", is(620))) // 440 + 180
                .andExpect(jsonPath("$.data.connectedPlatformsCount", is(2)))
                .andExpect(jsonPath("$.data.highestRating", is(1820.0)))
                .andExpect(jsonPath("$.data.highestRatingPlatform", is("LEETCODE")))
                .andExpect(jsonPath("$.data.totalContestsParticipated", is(25))) // 10 + 15
                .andExpect(jsonPath("$.data.activeSyncPlatformsCount", is(2)))
                .andExpect(jsonPath("$.data.syncHealthStatus", is("HEALTHY")))
                .andExpect(jsonPath("$.data.platformOverviews", hasSize(2)));
    }

    @Test
    @DisplayName("GET /overview — GitHub repos/stars and AtCoder community metrics are properly aggregated")
    void getOverview_WithGitHubAndAtCoder_AggregatesCorrectly() throws Exception {
        PlatformAccount gh = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.GITHUB)
                .username("alice_gh")
                .profileUrl("https://github.com/alice_gh")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(gh)
                .snapshotAt(Instant.now())
                .additionalStats(Map.of("publicRepos", 24, "totalStars", 145))
                .build());

        PlatformAccount atcoder = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.ATCODER)
                .username("alice_at")
                .profileUrl("https://atcoder.jp/users/alice_at")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(atcoder)
                .problemsSolved(95)
                .snapshotAt(Instant.now())
                .additionalStats(Map.of("communityAcRank", 3200, "ratedPointSum", 45000))
                .build());

        mockMvc.perform(get("/api/v1/coding/analytics/overview")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalGitHubRepositories", is(24)))
                .andExpect(jsonPath("$.data.totalGitHubStars", is(145)))
                .andExpect(jsonPath("$.data.activeSyncPlatformsCount", is(4))) // LC, CF, GH, ATCODER
                .andExpect(jsonPath("$.data.totalProblemsSolved", is(715))); // 620 + 95
    }

    @Test
    @DisplayName("GET /insights — Failed sync accounts trigger sync attention alerts")
    void getInsights_WithFailedSync_GeneratesAlert() throws Exception {
        PlatformAccount failing = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.LEETCODE)
                .username("alice_fail")
                .profileUrl("https://leetcode.com/u/alice_fail")
                .active(true)
                .syncStatus(SyncStatus.FAILED)
                .lastSyncError("External platform rate limit reached (HTTP 429)")
                .build());

        mockMvc.perform(get("/api/v1/coding/analytics/insights?period=30d")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.insights[?(@.category == 'SYNC_ALERT')]", not(empty())))
                .andExpect(jsonPath("$.data.insights[?(@.type == 'ATTENTION')]", not(empty())));
    }

    @Test
    @DisplayName("Tenant Isolation — Bob sees 0 connected accounts and empty analytics (IDOR Defense)")
    void getOverview_Bob_SeesOnlyOwnData() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/overview")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalProblemsSolved", is(0)))
                .andExpect(jsonPath("$.data.connectedPlatformsCount", is(0)))
                .andExpect(jsonPath("$.data.highestRating", nullValue()))
                .andExpect(jsonPath("$.data.platformOverviews", hasSize(0)));
    }

    @Test
    @DisplayName("GET /rating-history — Returns platform curve and period delta")
    void getRatingHistory_ReturnsPlatformHistory() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/rating-history?platform=LEETCODE&period=30d")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.platforms", hasSize(1)))
                .andExpect(jsonPath("$.data.platforms[0].platform", is("LEETCODE")))
                .andExpect(jsonPath("$.data.platforms[0].currentRating", is(1820.0)))
                .andExpect(jsonPath("$.data.platforms[0].ratingChange", is(70.0))) // 1820 - 1750
                .andExpect(jsonPath("$.data.platforms[0].history", hasSize(2)));
    }

    @Test
    @DisplayName("GET /problem-distribution — Returns difficulty breakdown and period growth")
    void getProblemDistribution_ReturnsDifficultyAndDelta() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/problem-distribution?period=30d")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalPlatformReportedSolved", is(620)))
                .andExpect(jsonPath("$.data.easySolved", is(160)))
                .andExpect(jsonPath("$.data.mediumSolved", is(220)))
                .andExpect(jsonPath("$.data.hardSolved", is(60)))
                .andExpect(jsonPath("$.data.unclassifiedSolved", is(180))) // Codeforces 180 solved without tiers
                .andExpect(jsonPath("$.data.solvedDeltaInPeriod", is(40))) // LeetCode grew 440 - 400 = 40
                .andExpect(jsonPath("$.data.verifiedUniqueDeduplication", is(false)));
    }

    @Test
    @DisplayName("GET /activity — Honestly reports periodic snapshot collection")
    void getActivity_ReturnsSnapshotTimeline() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/activity?period=30d")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.dailySubmissionActivitySupported", is(false)))
                .andExpect(jsonPath("$.data.totalSnapshotsInPeriod", is(3)))
                .andExpect(jsonPath("$.data.coverageNotice", notNullValue()));
    }

    @Test
    @DisplayName("GET /contest-performance — Returns contest stats for supported platforms")
    void getContestPerformance_ReturnsContestStats() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/contest-performance?period=30d")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalContestsParticipated", is(25)))
                .andExpect(jsonPath("$.data.platformContests", hasSize(2)));
    }

    @Test
    @DisplayName("GET /insights — Returns data-backed factual summaries")
    void getInsights_ReturnsFactualSummaries() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/insights?period=30d")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.insights", not(empty())))
                .andExpect(jsonPath("$.data.insights[?(@.title == 'Personal Best')]", not(empty())));
    }

    @Test
    @DisplayName("Security — Unauthenticated requests are rejected with 401 Unauthorized")
    void unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Validation — Invalid period string returns 400 Bad Request")
    void invalidPeriod_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/coding/analytics/overview?period=invalid_period")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk()); // overview doesn't use period

        mockMvc.perform(get("/api/v1/coding/analytics/rating-history?period=invalid_period")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isBadRequest());
    }
}
