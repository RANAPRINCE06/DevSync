package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeetCodeAdapterTest — LeetCode GraphQL API Integration & Metric Normalization")
class LeetCodeAdapterTest {

    @Mock
    private CodingPlatformClient client;

    private LeetCodeAdapter adapter;

    @BeforeEach
    void setUp() {
        CodingIntegrationProperties properties = new CodingIntegrationProperties();
        properties.getLeetcode().setBaseUrl("https://leetcode.com/graphql");
        adapter = new LeetCodeAdapter(client, properties);
    }

    @Test
    @DisplayName("Platform metadata — Reports LEETCODE")
    void getPlatform_ReturnsLeetCode() {
        assertEquals(CodingPlatform.LEETCODE, adapter.getPlatform());
        assertTrue(adapter.supports(CodingPlatform.LEETCODE));
        assertFalse(adapter.supports(CodingPlatform.GITHUB));
    }

    @Test
    @DisplayName("Fetch profile — Success with problem breakdown and contest rating")
    void fetchProfileData_Success() {
        LeetCodeAdapter.LeetCodeGraphQLResponse response = new LeetCodeAdapter.LeetCodeGraphQLResponse();
        response.data = new LeetCodeAdapter.LeetCodeData();

        LeetCodeAdapter.LeetCodeMatchedUser user = new LeetCodeAdapter.LeetCodeMatchedUser();
        user.username = "alice_dev";
        user.profile = new LeetCodeAdapter.LeetCodeProfile();
        user.profile.ranking = 15000;
        user.profile.reputation = 250;

        user.submitStatsGlobal = new LeetCodeAdapter.LeetCodeSubmitStats();
        user.submitStatsGlobal.acSubmissionNum = new ArrayList<>();

        LeetCodeAdapter.LeetCodeSubmissionCount all = new LeetCodeAdapter.LeetCodeSubmissionCount();
        all.difficulty = "All";
        all.count = 450;

        LeetCodeAdapter.LeetCodeSubmissionCount easy = new LeetCodeAdapter.LeetCodeSubmissionCount();
        easy.difficulty = "Easy";
        easy.count = 200;

        LeetCodeAdapter.LeetCodeSubmissionCount medium = new LeetCodeAdapter.LeetCodeSubmissionCount();
        medium.difficulty = "Medium";
        medium.count = 180;

        LeetCodeAdapter.LeetCodeSubmissionCount hard = new LeetCodeAdapter.LeetCodeSubmissionCount();
        hard.difficulty = "Hard";
        hard.count = 70;

        user.submitStatsGlobal.acSubmissionNum.addAll(List.of(all, easy, medium, hard));
        response.data.matchedUser = user;

        LeetCodeAdapter.LeetCodeContestRanking contest = new LeetCodeAdapter.LeetCodeContestRanking();
        contest.rating = 1875.45;
        contest.globalRanking = 12000;
        contest.attendedContestsCount = 28;
        contest.topPercentage = 5.2;
        contest.badge = new LeetCodeAdapter.LeetCodeBadge();
        contest.badge.name = "Knight";
        response.data.userContestRanking = contest;

        when(client.post(eq("https://leetcode.com/graphql"), any(), eq(LeetCodeAdapter.LeetCodeGraphQLResponse.class), anyMap()))
                .thenReturn(response);

        ExternalProfileData data = adapter.fetchProfileData("alice_dev");

        assertNotNull(data);
        assertEquals(CodingPlatform.LEETCODE, data.getPlatform());
        assertEquals("alice_dev", data.getUsername());
        assertEquals("https://leetcode.com/u/alice_dev/", data.getProfileUrl());

        // Problems solved breakdown
        assertEquals(450, data.getProblemsSolved());
        assertEquals(200, data.getEasySolved());
        assertEquals(180, data.getMediumSolved());
        assertEquals(70, data.getHardSolved());

        // Contest metrics
        assertEquals(1875.45, data.getCurrentRating());
        assertEquals(12000L, data.getGlobalRank());
        assertEquals(28, data.getContestsParticipated());
        assertEquals("Knight", data.getPlatformRank());

        assertNotNull(data.getAdditionalStats());
        assertEquals(5.2, data.getAdditionalStats().get("topPercentage"));
        assertEquals(250, data.getAdditionalStats().get("reputation"));
    }

    @Test
    @DisplayName("Fetch profile — User with no contest history returns null contest metrics")
    void fetchProfileData_NoContests_NullContestMetrics() {
        LeetCodeAdapter.LeetCodeGraphQLResponse response = new LeetCodeAdapter.LeetCodeGraphQLResponse();
        response.data = new LeetCodeAdapter.LeetCodeData();

        LeetCodeAdapter.LeetCodeMatchedUser user = new LeetCodeAdapter.LeetCodeMatchedUser();
        user.username = "practice_only_user";
        user.profile = new LeetCodeAdapter.LeetCodeProfile();
        user.profile.ranking = 150000;

        user.submitStatsGlobal = new LeetCodeAdapter.LeetCodeSubmitStats();
        LeetCodeAdapter.LeetCodeSubmissionCount all = new LeetCodeAdapter.LeetCodeSubmissionCount();
        all.difficulty = "All";
        all.count = 50;
        user.submitStatsGlobal.acSubmissionNum = List.of(all);

        response.data.matchedUser = user;
        response.data.userContestRanking = null; // User never joined a contest

        when(client.post(eq("https://leetcode.com/graphql"), any(), eq(LeetCodeAdapter.LeetCodeGraphQLResponse.class), anyMap()))
                .thenReturn(response);

        ExternalProfileData data = adapter.fetchProfileData("practice_only_user");

        assertEquals(50, data.getProblemsSolved());
        assertNull(data.getCurrentRating(), "Rating must be null when user has no contest history");
        assertNull(data.getMaximumRating());
        assertNull(data.getContestsParticipated());
        assertEquals(150000L, data.getGlobalRank()); // Uses profile.ranking as fallback
    }

    @Test
    @DisplayName("Fetch profile with non-existent handle — Throws PlatformUserNotFoundException")
    void fetchProfileData_UserNotFound_ThrowsException() {
        LeetCodeAdapter.LeetCodeGraphQLResponse response = new LeetCodeAdapter.LeetCodeGraphQLResponse();
        response.data = new LeetCodeAdapter.LeetCodeData();
        response.data.matchedUser = null;

        when(client.post(eq("https://leetcode.com/graphql"), any(), eq(LeetCodeAdapter.LeetCodeGraphQLResponse.class), anyMap()))
                .thenReturn(response);

        assertThrows(PlatformUserNotFoundException.class, () ->
                adapter.fetchProfileData("non_existent_lc_user")
        );
    }

    @Test
    @DisplayName("Fetch profile when GraphQL returns error — Throws PlatformIntegrationException")
    void fetchProfileData_GraphQLError_ThrowsException() {
        LeetCodeAdapter.LeetCodeGraphQLResponse response = new LeetCodeAdapter.LeetCodeGraphQLResponse();
        LeetCodeAdapter.LeetCodeError error = new LeetCodeAdapter.LeetCodeError();
        error.message = "Rate limit exceeded on GraphQL endpoint";
        response.errors = List.of(error);

        when(client.post(eq("https://leetcode.com/graphql"), any(), eq(LeetCodeAdapter.LeetCodeGraphQLResponse.class), anyMap()))
                .thenReturn(response);

        assertThrows(PlatformIntegrationException.class, () ->
                adapter.fetchProfileData("rate_limited_user")
        );
    }
}
