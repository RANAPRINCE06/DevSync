package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformRateLimitException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitHubAdapterTest — GitHub REST API Integration & Metric Normalization")
class GitHubAdapterTest {

    @Mock
    private CodingPlatformClient client;

    private GitHubAdapter adapter;

    @BeforeEach
    void setUp() {
        CodingIntegrationProperties properties = new CodingIntegrationProperties();
        properties.getGithub().setBaseUrl("https://api.github.com");
        adapter = new GitHubAdapter(client, properties);
    }

    @Test
    @DisplayName("Platform metadata — Reports GITHUB")
    void getPlatform_ReturnsGitHub() {
        assertEquals(CodingPlatform.GITHUB, adapter.getPlatform());
        assertTrue(adapter.supports(CodingPlatform.GITHUB));
        assertFalse(adapter.supports(CodingPlatform.CODEFORCES));
    }

    @Test
    @DisplayName("Fetch profile — Success with stars sum and developer metadata")
    void fetchProfileData_Success() {
        GitHubAdapter.GitHubUserResponse user = new GitHubAdapter.GitHubUserResponse();
        user.login = "octocat";
        user.name = "The Octocat";
        user.publicRepos = 8;
        user.publicGists = 2;
        user.followers = 1000;
        user.following = 10;
        user.bio = "GitHub Mascot";
        user.company = "GitHub";
        user.location = "San Francisco";
        user.createdAt = "2011-01-25T18:44:36Z";

        when(client.get(eq("https://api.github.com/users/octocat"), eq(GitHubAdapter.GitHubUserResponse.class), anyMap()))
                .thenReturn(user);

        GitHubAdapter.GitHubRepo repo1 = new GitHubAdapter.GitHubRepo();
        repo1.stargazersCount = 150;
        repo1.forksCount = 20;

        GitHubAdapter.GitHubRepo repo2 = new GitHubAdapter.GitHubRepo();
        repo2.stargazersCount = 350;
        repo2.forksCount = 45;

        when(client.get(contains("/repos"), eq(GitHubAdapter.GitHubRepo[].class), anyMap()))
                .thenReturn(new GitHubAdapter.GitHubRepo[]{repo1, repo2});

        ExternalProfileData data = adapter.fetchProfileData("octocat");

        assertNotNull(data);
        assertEquals(CodingPlatform.GITHUB, data.getPlatform());
        assertEquals("octocat", data.getUsername());
        assertEquals("https://github.com/octocat", data.getProfileUrl());

        // Invariants: GitHub is code hosting, NOT a competitive programming platform
        // All CP rating, ranking and problem solving metrics MUST remain strictly null!
        assertNull(data.getCurrentRating(), "GitHub rating must be null");
        assertNull(data.getMaximumRating(), "GitHub max rating must be null");
        assertNull(data.getGlobalRank(), "GitHub global rank must be null");
        assertNull(data.getCountryRank(), "GitHub country rank must be null");
        assertNull(data.getProblemsSolved(), "GitHub problems solved must be null");
        assertNull(data.getContestsParticipated(), "GitHub contests must be null");

        // Additional stats
        assertNotNull(data.getAdditionalStats());
        assertEquals(8, data.getAdditionalStats().get("publicRepos"));
        assertEquals(1000, data.getAdditionalStats().get("followers"));
        assertEquals(500, data.getAdditionalStats().get("totalStars")); // 150 + 350
        assertEquals(65, data.getAdditionalStats().get("totalForks"));   // 20 + 45
        assertEquals("GitHub", data.getAdditionalStats().get("company"));
    }

    @Test
    @DisplayName("Fetch profile with non-existent user — Propagates PlatformUserNotFoundException")
    void fetchProfileData_UserNotFound_ThrowsException() {
        when(client.get(contains("/users/unknown-dev-404"), eq(GitHubAdapter.GitHubUserResponse.class), anyMap()))
                .thenThrow(new PlatformUserNotFoundException("Profile not found"));

        assertThrows(PlatformUserNotFoundException.class, () ->
                adapter.fetchProfileData("unknown-dev-404")
        );
    }

    @Test
    @DisplayName("Fetch profile when rate-limited — Propagates PlatformRateLimitException")
    void fetchProfileData_RateLimit_ThrowsException() {
        when(client.get(contains("/users/busy-dev"), eq(GitHubAdapter.GitHubUserResponse.class), anyMap()))
                .thenThrow(new PlatformRateLimitException("GitHub API rate limit exhausted"));

        assertThrows(PlatformRateLimitException.class, () ->
                adapter.fetchProfileData("busy-dev")
        );
    }

    @Test
    @DisplayName("Validate username — Rejects invalid GitHub handle characters")
    void fetchProfileData_InvalidUsername_ThrowsException() {
        assertThrows(PlatformIntegrationException.class, () ->
                adapter.fetchProfileData("-invalid_start-")
        );
    }
}
