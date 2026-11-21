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
@DisplayName("CodeforcesAdapterTest — Codeforces REST API Integration & Normalization")
class CodeforcesAdapterTest {

    @Mock
    private CodingPlatformClient client;

    private CodeforcesAdapter adapter;

    @BeforeEach
    void setUp() {
        CodingIntegrationProperties properties = new CodingIntegrationProperties();
        properties.getCodeforces().setBaseUrl("https://codeforces.com/api");
        adapter = new CodeforcesAdapter(client, properties);
    }

    @Test
    @DisplayName("Platform metadata — Reports CODEFORCES")
    void getPlatform_ReturnsCodeforces() {
        assertEquals(CodingPlatform.CODEFORCES, adapter.getPlatform());
        assertTrue(adapter.supports(CodingPlatform.CODEFORCES));
        assertFalse(adapter.supports(CodingPlatform.LEETCODE));
    }

    @Test
    @DisplayName("Fetch profile — Successful retrieval with ratings, rank, and unique accepted submissions")
    void fetchProfileData_Success() {
        // Mock user.info
        CodeforcesAdapter.CodeforcesUserInfoResponse infoResponse = new CodeforcesAdapter.CodeforcesUserInfoResponse();
        infoResponse.status = "OK";
        CodeforcesAdapter.CodeforcesUser user = new CodeforcesAdapter.CodeforcesUser();
        user.handle = "tourist";
        user.rating = 3850;
        user.maxRating = 4009;
        user.rank = "legendary grandmaster";
        user.maxRank = "tourist";
        user.contribution = 150;
        user.country = "Belarus";
        infoResponse.result = List.of(user);

        when(client.get(contains("user.info"), eq(CodeforcesAdapter.CodeforcesUserInfoResponse.class), isNull()))
                .thenReturn(infoResponse);

        // Mock user.rating (contests)
        CodeforcesAdapter.CodeforcesRatingResponse ratingResponse = new CodeforcesAdapter.CodeforcesRatingResponse();
        ratingResponse.status = "OK";
        ratingResponse.result = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            ratingResponse.result.add(new CodeforcesAdapter.CodeforcesRatingChange());
        }
        when(client.get(contains("user.rating"), eq(CodeforcesAdapter.CodeforcesRatingResponse.class), isNull()))
                .thenReturn(ratingResponse);

        // Mock user.status (submissions: 3 OK submissions with 2 unique problems)
        CodeforcesAdapter.CodeforcesStatusResponse statusResponse = new CodeforcesAdapter.CodeforcesStatusResponse();
        statusResponse.status = "OK";

        CodeforcesAdapter.CodeforcesSubmission sub1 = new CodeforcesAdapter.CodeforcesSubmission();
        sub1.verdict = "OK";
        sub1.problem = new CodeforcesAdapter.CodeforcesProblem();
        sub1.problem.contestId = 1000;
        sub1.problem.index = "A";

        CodeforcesAdapter.CodeforcesSubmission sub2 = new CodeforcesAdapter.CodeforcesSubmission();
        sub2.verdict = "OK";
        sub2.problem = new CodeforcesAdapter.CodeforcesProblem();
        sub2.problem.contestId = 1000;
        sub2.problem.index = "A"; // Duplicate submission of problem 1000A

        CodeforcesAdapter.CodeforcesSubmission sub3 = new CodeforcesAdapter.CodeforcesSubmission();
        sub3.verdict = "WRONG_ANSWER";
        sub3.problem = new CodeforcesAdapter.CodeforcesProblem();
        sub3.problem.contestId = 1000;
        sub3.problem.index = "B";

        CodeforcesAdapter.CodeforcesSubmission sub4 = new CodeforcesAdapter.CodeforcesSubmission();
        sub4.verdict = "OK";
        sub4.problem = new CodeforcesAdapter.CodeforcesProblem();
        sub4.problem.contestId = 1000;
        sub4.problem.index = "C";

        statusResponse.result = List.of(sub1, sub2, sub3, sub4);

        when(client.get(contains("user.status"), eq(CodeforcesAdapter.CodeforcesStatusResponse.class), isNull()))
                .thenReturn(statusResponse);

        ExternalProfileData data = adapter.fetchProfileData("tourist");

        assertNotNull(data);
        assertEquals(CodingPlatform.CODEFORCES, data.getPlatform());
        assertEquals("tourist", data.getUsername());
        assertEquals(3850.0, data.getCurrentRating());
        assertEquals(4009.0, data.getMaximumRating());
        assertEquals("legendary grandmaster", data.getPlatformRank());
        assertEquals(42, data.getContestsParticipated());

        // Deduplication invariant: 2 unique problems solved (1000A, 1000C) out of 3 OK submissions
        assertEquals(2, data.getProblemsSolved());
        assertNull(data.getGlobalRank()); // Codeforces does not provide global rank
        assertNull(data.getCountryRank());

        assertNotNull(data.getAdditionalStats());
        assertEquals(150, data.getAdditionalStats().get("contribution"));
        assertEquals("Belarus", data.getAdditionalStats().get("country"));
        assertEquals(3, data.getAdditionalStats().get("rawAcceptedSubmissions"));
    }

    @Test
    @DisplayName("Fetch profile — Unrated user returns null ratings and 0 contests")
    void fetchProfileData_UnratedUser_NullRatings() {
        CodeforcesAdapter.CodeforcesUserInfoResponse infoResponse = new CodeforcesAdapter.CodeforcesUserInfoResponse();
        infoResponse.status = "OK";
        CodeforcesAdapter.CodeforcesUser user = new CodeforcesAdapter.CodeforcesUser();
        user.handle = "newbie_coder";
        user.rating = null;
        user.maxRating = null;
        user.rank = null;
        infoResponse.result = List.of(user);

        when(client.get(contains("user.info"), eq(CodeforcesAdapter.CodeforcesUserInfoResponse.class), isNull()))
                .thenReturn(infoResponse);

        CodeforcesAdapter.CodeforcesRatingResponse ratingResponse = new CodeforcesAdapter.CodeforcesRatingResponse();
        ratingResponse.status = "OK";
        ratingResponse.result = List.of();
        when(client.get(contains("user.rating"), eq(CodeforcesAdapter.CodeforcesRatingResponse.class), isNull()))
                .thenReturn(ratingResponse);

        CodeforcesAdapter.CodeforcesStatusResponse statusResponse = new CodeforcesAdapter.CodeforcesStatusResponse();
        statusResponse.status = "OK";
        statusResponse.result = List.of();
        when(client.get(contains("user.status"), eq(CodeforcesAdapter.CodeforcesStatusResponse.class), isNull()))
                .thenReturn(statusResponse);

        ExternalProfileData data = adapter.fetchProfileData("newbie_coder");

        assertNull(data.getCurrentRating());
        assertNull(data.getMaximumRating());
        assertEquals(0, data.getContestsParticipated());
        assertEquals(0, data.getProblemsSolved());
    }

    @Test
    @DisplayName("Fetch profile with non-existent handle — Throws PlatformUserNotFoundException")
    void fetchProfileData_UserNotFound_ThrowsException() {
        CodeforcesAdapter.CodeforcesUserInfoResponse errorResponse = new CodeforcesAdapter.CodeforcesUserInfoResponse();
        errorResponse.status = "FAILED";
        errorResponse.comment = "handles: User with handle non_existent_user_123 not found";

        when(client.get(contains("user.info"), eq(CodeforcesAdapter.CodeforcesUserInfoResponse.class), isNull()))
                .thenReturn(errorResponse);

        assertThrows(PlatformUserNotFoundException.class, () ->
                adapter.fetchProfileData("non_existent_user_123")
        );
    }

    @Test
    @DisplayName("Validate username — Invalid username format throws exception")
    void fetchProfileData_InvalidUsername_ThrowsException() {
        assertThrows(PlatformIntegrationException.class, () ->
                adapter.fetchProfileData("bad handle with spaces!")
        );
    }
}
