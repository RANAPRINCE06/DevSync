package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.DefaultCodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformRateLimitException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformAdapterFaultToleranceTest — Comprehensive Adapter & Client Resilience Audit")
class PlatformAdapterFaultToleranceTest {

    private CodingIntegrationProperties properties;

    @BeforeEach
    void setUpCommon() {
        properties = new CodingIntegrationProperties();
        properties.getCodeforces().setBaseUrl("https://codeforces.com/api");
        properties.getLeetcode().setBaseUrl("https://leetcode.com/graphql");
        properties.getGithub().setBaseUrl("https://api.github.com");
    }

    // ==========================================
    // 1. CODEFORCES FAULT TOLERANCE
    // ==========================================
    @Nested
    @DisplayName("Codeforces Adapter Fault Tolerance")
    class CodeforcesFaultTolerance {

        @Mock
        private CodingPlatformClient mockClient;

        private CodeforcesAdapter adapter;

        @BeforeEach
        void setUp() {
            adapter = new CodeforcesAdapter(mockClient, properties);
        }

        @Test
        @DisplayName("Handles null/missing optional fields without NullPointerException")
        void handlesNullOptionalFields() {
            CodeforcesAdapter.CodeforcesUserInfoResponse infoResponse = new CodeforcesAdapter.CodeforcesUserInfoResponse();
            infoResponse.status = "OK";
            CodeforcesAdapter.CodeforcesUser user = new CodeforcesAdapter.CodeforcesUser();
            user.handle = "new_coder";
            user.rating = null; // Unrated user
            user.maxRating = null;
            user.rank = null;
            user.maxRank = null;
            user.contribution = null;
            infoResponse.result = List.of(user);

            when(mockClient.get(contains("user.info"), eq(CodeforcesAdapter.CodeforcesUserInfoResponse.class), isNull()))
                    .thenReturn(infoResponse);
            when(mockClient.get(contains("user.rating"), eq(CodeforcesAdapter.CodeforcesRatingResponse.class), isNull()))
                    .thenReturn(null);
            when(mockClient.get(contains("user.status"), eq(CodeforcesAdapter.CodeforcesStatusResponse.class), isNull()))
                    .thenReturn(null);

            ExternalProfileData data = adapter.fetchProfileData("new_coder");
            assertNotNull(data);
            assertEquals("new_coder", data.getUsername());
            assertNull(data.getCurrentRating());
            assertNull(data.getGlobalRank());
            assertNull(data.getProblemsSolved());
            assertNull(data.getContestsParticipated());
        }

        @Test
        @DisplayName("Propagates PlatformRateLimitException when external rate limit is hit")
        void propagatesRateLimitException() {
            when(mockClient.get(contains("user.info"), any(), any()))
                    .thenThrow(new PlatformRateLimitException("CF Rate Limit Exceeded", 60L));

            PlatformRateLimitException ex = assertThrows(PlatformRateLimitException.class,
                    () -> adapter.fetchProfileData("tourist"));
            assertEquals(60L, ex.getRetryAfterSeconds());
        }

        @Test
        @DisplayName("Throws PlatformUserNotFoundException on Codeforces user not found response")
        void throwsPlatformUserNotFound() {
            CodeforcesAdapter.CodeforcesUserInfoResponse failedResponse = new CodeforcesAdapter.CodeforcesUserInfoResponse();
            failedResponse.status = "FAILED";
            failedResponse.comment = "handles: User not found";

            when(mockClient.get(contains("user.info"), any(), any()))
                    .thenReturn(failedResponse);

            assertThrows(PlatformUserNotFoundException.class,
                    () -> adapter.fetchProfileData("non_existent_handle_xyz"));
        }

        @Test
        @DisplayName("Throws PlatformIntegrationException on 500/502/503 server error")
        void propagatesServerError() {
            when(mockClient.get(contains("user.info"), any(), any()))
                    .thenThrow(new PlatformIntegrationException("External platform server unavailable: HTTP 503"));

            PlatformIntegrationException ex = assertThrows(PlatformIntegrationException.class,
                    () -> adapter.fetchProfileData("tourist"));
            assertTrue(ex.getMessage().contains("503"));
        }

        @Test
        @DisplayName("Propagates network timeout without leaking internal credentials")
        void propagatesNetworkTimeoutSanitized() {
            when(mockClient.get(contains("user.info"), any(), any()))
                    .thenThrow(new PlatformIntegrationException("External platform connection timed out or unreachable"));

            PlatformIntegrationException ex = assertThrows(PlatformIntegrationException.class,
                    () -> adapter.fetchProfileData("tourist"));
            assertFalse(ex.getMessage().contains("password"));
            assertFalse(ex.getMessage().contains("secret"));
            assertFalse(ex.getMessage().contains("token"));
        }
    }

    // ==========================================
    // 2. LEETCODE FAULT TOLERANCE
    // ==========================================
    @Nested
    @DisplayName("LeetCode Adapter Fault Tolerance")
    class LeetCodeFaultTolerance {

        @Mock
        private CodingPlatformClient mockClient;

        private LeetCodeAdapter adapter;

        @BeforeEach
        void setUp() {
            adapter = new LeetCodeAdapter(mockClient, properties);
        }

        @Test
        @DisplayName("Handles unrated user with missing contest ranking and partial submission stats")
        void handlesNullContestAndRanking() {
            LeetCodeAdapter.LeetCodeGraphQLResponse response = new LeetCodeAdapter.LeetCodeGraphQLResponse();
            response.data = new LeetCodeAdapter.LeetCodeData();
            response.data.matchedUser = new LeetCodeAdapter.LeetCodeMatchedUser();
            response.data.matchedUser.username = "new_leetcode_dev";
            response.data.matchedUser.profile = new LeetCodeAdapter.LeetCodeProfile();
            response.data.matchedUser.profile.ranking = null;
            response.data.matchedUser.profile.reputation = null;
            response.data.userContestRanking = null; // Unrated in contests

            LeetCodeAdapter.LeetCodeSubmitStats stats = new LeetCodeAdapter.LeetCodeSubmitStats();
            LeetCodeAdapter.LeetCodeSubmissionCount sub = new LeetCodeAdapter.LeetCodeSubmissionCount();
            sub.difficulty = "All";
            sub.count = 45;
            stats.acSubmissionNum = List.of(sub);
            response.data.matchedUser.submitStatsGlobal = stats;

            when(mockClient.post(anyString(), any(), eq(LeetCodeAdapter.LeetCodeGraphQLResponse.class), any()))
                    .thenReturn(response);

            ExternalProfileData data = adapter.fetchProfileData("new_leetcode_dev");
            assertNotNull(data);
            assertEquals("new_leetcode_dev", data.getUsername());
            assertEquals(45, data.getProblemsSolved());
            assertNull(data.getCurrentRating());
            assertNull(data.getGlobalRank());
            assertNull(data.getContestsParticipated());
        }

        @Test
        @DisplayName("Maps missing matchedUser to PlatformUserNotFoundException")
        void mapsMissingUserToUserNotFound() {
            LeetCodeAdapter.LeetCodeGraphQLResponse response = new LeetCodeAdapter.LeetCodeGraphQLResponse();
            response.data = new LeetCodeAdapter.LeetCodeData();
            response.data.matchedUser = null;

            when(mockClient.post(anyString(), any(), eq(LeetCodeAdapter.LeetCodeGraphQLResponse.class), any()))
                    .thenReturn(response);

            assertThrows(PlatformUserNotFoundException.class,
                    () -> adapter.fetchProfileData("non_existent_lc_user"));
        }

        @Test
        @DisplayName("Handles GraphQL errors payload gracefully")
        void handlesGraphQLErrors() {
            LeetCodeAdapter.LeetCodeGraphQLResponse response = new LeetCodeAdapter.LeetCodeGraphQLResponse();
            LeetCodeAdapter.LeetCodeError err = new LeetCodeAdapter.LeetCodeError();
            err.message = "Rate limit exceeded on GraphQL query";
            response.errors = List.of(err);

            when(mockClient.post(anyString(), any(), eq(LeetCodeAdapter.LeetCodeGraphQLResponse.class), any()))
                    .thenReturn(response);

            PlatformIntegrationException ex = assertThrows(PlatformIntegrationException.class,
                    () -> adapter.fetchProfileData("some_user"));
            assertTrue(ex.getMessage().contains("Rate limit exceeded"));
        }
    }

    // ==========================================
    // 3. GITHUB FAULT TOLERANCE
    // ==========================================
    @Nested
    @DisplayName("GitHub Adapter Fault Tolerance")
    class GitHubFaultTolerance {

        @Mock
        private CodingPlatformClient mockClient;

        private GitHubAdapter adapter;

        @BeforeEach
        void setUp() {
            adapter = new GitHubAdapter(mockClient, properties);
        }

        @Test
        @DisplayName("Handles missing repositories or repo fetch failure gracefully")
        void handlesRepoFetchFailureGracefully() {
            GitHubAdapter.GitHubUserResponse user = new GitHubAdapter.GitHubUserResponse();
            user.login = "octocat";
            user.publicRepos = 10;
            user.followers = 500;
            user.following = 10;

            when(mockClient.get(contains("/users/octocat"), eq(GitHubAdapter.GitHubUserResponse.class), any()))
                    .thenReturn(user);
            // Repos endpoint fails with 500 or timeout
            when(mockClient.get(contains("/users/octocat/repos"), eq(GitHubAdapter.GitHubRepo[].class), any()))
                    .thenThrow(new PlatformIntegrationException("Repo fetch timed out"));

            ExternalProfileData data = adapter.fetchProfileData("octocat");
            assertNotNull(data);
            assertEquals("octocat", data.getUsername());
            assertEquals(10, data.getAdditionalStats().get("publicRepos"));
            assertNull(data.getAdditionalStats().get("totalStars")); // Repos failed, stays null without crashing
        }

        @Test
        @DisplayName("Validates username format to prevent injection")
        void validatesUsernameFormat() {
            assertFalse(adapter.validateUsername("-invalid-start"));
            assertFalse(adapter.validateUsername("invalid--double-dash"));
            assertFalse(adapter.validateUsername("user name with spaces"));
            assertTrue(adapter.validateUsername("valid-user-123"));

            assertThrows(PlatformIntegrationException.class,
                    () -> adapter.fetchProfileData("invalid user space"));
        }
    }

    // ==========================================
    // 4. CODECHEF, HACKERRANK & ATCODER BOUNDARY
    // ==========================================
    @Nested
    @DisplayName("Unsupported Platform Adapters Safety Boundaries")
    class UnsupportedPlatformFaultTolerance {

        @Test
        @DisplayName("CodeChef throws PlatformUnsupportedException safely without scraping")
        void codeChefThrowsUnsupportedSafely() {
            CodeChefAdapter adapter = new CodeChefAdapter(properties);
            assertEquals(CodingPlatform.CODECHEF, adapter.getPlatform());
            assertTrue(adapter.validateUsername("codechef_coder"));

            PlatformUnsupportedException ex = assertThrows(PlatformUnsupportedException.class,
                    () -> adapter.fetchProfileData("codechef_coder"));
            assertTrue(ex.getMessage().contains("OAuth2") || ex.getMessage().contains("scraping"));
        }

        @Test
        @DisplayName("HackerRank throws PlatformUnsupportedException safely without scraping")
        void hackerRankThrowsUnsupportedSafely() {
            HackerRankAdapter adapter = new HackerRankAdapter(properties);
            assertEquals(CodingPlatform.HACKERRANK, adapter.getPlatform());
            assertTrue(adapter.validateUsername("hackerrank_coder"));

            PlatformUnsupportedException ex = assertThrows(PlatformUnsupportedException.class,
                    () -> adapter.fetchProfileData("hackerrank_coder"));
            assertTrue(ex.getMessage().contains("HackerRank"));
        }

        @Test
        @DisplayName("AtCoder throws PlatformUnsupportedException safely without scraping")
        void atCoderThrowsUnsupportedSafely() {
            AtCoderAdapter adapter = new AtCoderAdapter(properties);
            assertEquals(CodingPlatform.ATCODER, adapter.getPlatform());
            assertTrue(adapter.validateUsername("chokudai"));

            PlatformUnsupportedException ex = assertThrows(PlatformUnsupportedException.class,
                    () -> adapter.fetchProfileData("chokudai"));
            assertTrue(ex.getMessage().contains("AtCoder"));
        }
    }

    // ==========================================
    // 5. DEFAULT CODING PLATFORM CLIENT RESILIENCE
    // ==========================================
    @Nested
    @DisplayName("DefaultCodingPlatformClient HTTP Resilience & Header Parsing")
    class HttpClientResilience {

        private RestClient.Builder restClientBuilder;
        private MockRestServiceServer mockServer;
        private DefaultCodingPlatformClient client;

        @BeforeEach
        void setUp() {
            restClientBuilder = RestClient.builder();
            mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
            client = new DefaultCodingPlatformClient(restClientBuilder.build());
        }

        @Test
        @DisplayName("Parses HTTP 429 Retry-After header and raises PlatformRateLimitException")
        void parsesHttp429WithRetryAfter() {
            mockServer.expect(requestTo("https://api.example.com/rate-limited"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                            .header(HttpHeaders.RETRY_AFTER, "45")
                            .body("{\"message\":\"Too many requests\"}")
                            .contentType(MediaType.APPLICATION_JSON));

            PlatformRateLimitException ex = assertThrows(PlatformRateLimitException.class,
                    () -> client.get("https://api.example.com/rate-limited", String.class, null));

            assertEquals(45L, ex.getRetryAfterSeconds());
            mockServer.verify();
        }

        @Test
        @DisplayName("Maps HTTP 404 to PlatformUserNotFoundException")
        void mapsHttp404ToUserNotFound() {
            mockServer.expect(requestTo("https://api.example.com/users/ghost"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .body("{\"message\":\"Not Found\"}")
                            .contentType(MediaType.APPLICATION_JSON));

            assertThrows(PlatformUserNotFoundException.class,
                    () -> client.get("https://api.example.com/users/ghost", String.class, null));

            mockServer.verify();
        }

        @Test
        @DisplayName("Maps HTTP 500, 502, 503 to PlatformIntegrationException")
        void maps5xxToPlatformIntegrationException() {
            mockServer.expect(requestTo("https://api.example.com/gateway-error"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                            .body("Bad Gateway"));

            PlatformIntegrationException ex = assertThrows(PlatformIntegrationException.class,
                    () -> client.get("https://api.example.com/gateway-error", String.class, null));

            assertTrue(ex.getMessage().contains("502"));
            mockServer.verify();
        }

        @Test
        @DisplayName("Sanitizes sensitive information from exception messages")
        void sanitizesExceptionMessages() {
            mockServer.expect(requestTo("https://api.example.com/secured"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                            .body("Unauthorized access to token"));

            PlatformIntegrationException ex = assertThrows(PlatformIntegrationException.class,
                    () -> client.get("https://api.example.com/secured", String.class, Map.of("Authorization", "Bearer secret_jwt_12345")));

            assertFalse(ex.getMessage().contains("secret_jwt_12345"), "Exception message must not leak bearer token");
            mockServer.verify();
        }
    }
}
