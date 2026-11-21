package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class GitHubAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(GitHubAdapter.class);

    private final CodingPlatformClient client;
    private final String baseUrl;

    public GitHubAdapter(CodingPlatformClient client, CodingIntegrationProperties properties) {
        this.client = client;
        this.baseUrl = properties.getGithub().getBaseUrl();
    }

    @Override
    public CodingPlatform getPlatform() {
        return CodingPlatform.GITHUB;
    }

    @Override
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$");
    }

    @Override
    public ExternalProfileData fetchProfileData(String username) {
        if (!validateUsername(username)) {
            throw new PlatformIntegrationException("Invalid GitHub username format: " + username);
        }

        Map<String, String> headers = Map.of(
                "Accept", "application/vnd.github.v3+json",
                "User-Agent", "DevSync-CodingIntelligence/1.0"
        );

        // 1. Fetch User Profile (/users/{username})
        String userUrl = baseUrl + "/users/" + username;
        GitHubUserResponse user = client.get(userUrl, GitHubUserResponse.class, headers);

        if (user == null || user.login == null) {
            throw new PlatformIntegrationException("Empty response received from GitHub API for: " + username);
        }

        // 2. Fetch Public Repositories (/users/{username}/repos?per_page=100) for star count
        Integer totalStars = null;
        Integer totalForks = null;
        try {
            String reposUrl = baseUrl + "/users/" + username + "/repos?per_page=100&type=owner&sort=updated";
            GitHubRepo[] repos = client.get(reposUrl, GitHubRepo[].class, headers);
            if (repos != null) {
                int stars = 0;
                int forks = 0;
                for (GitHubRepo repo : repos) {
                    if (repo.stargazersCount != null) stars += repo.stargazersCount;
                    if (repo.forksCount != null) forks += repo.forksCount;
                }
                totalStars = stars;
                totalForks = forks;
            }
        } catch (Exception ex) {
            log.debug("Could not calculate GitHub stars for {}: {}", username, ex.getMessage());
        }

        // 3. Assemble Additional Statistics Map
        Map<String, Object> additionalStats = new LinkedHashMap<>();
        if (user.publicRepos != null) additionalStats.put("publicRepos", user.publicRepos);
        if (user.publicGists != null) additionalStats.put("publicGists", user.publicGists);
        if (user.followers != null) additionalStats.put("followers", user.followers);
        if (user.following != null) additionalStats.put("following", user.following);
        if (totalStars != null) additionalStats.put("totalStars", totalStars);
        if (totalForks != null) additionalStats.put("totalForks", totalForks);
        if (user.company != null) additionalStats.put("company", user.company);
        if (user.location != null) additionalStats.put("location", user.location);
        if (user.bio != null) additionalStats.put("bio", user.bio);
        if (user.createdAt != null) additionalStats.put("accountCreatedAt", user.createdAt);

        String profileUrl = CodingPlatform.GITHUB.buildProfileUrl(username);

        // GitHub has no contest rating, global rank, or CP problem solving stats.
        // Invariants: missing/incompatible stats MUST remain null.
        return ExternalProfileData.builder()
                .platform(CodingPlatform.GITHUB)
                .username(username)
                .profileUrl(profileUrl)
                .currentRating(null)
                .maximumRating(null)
                .globalRank(null)
                .countryRank(null)
                .problemsSolved(null)
                .easySolved(null)
                .mediumSolved(null)
                .hardSolved(null)
                .contestsParticipated(null)
                .platformRank(null)
                .additionalStats(additionalStats.isEmpty() ? null : additionalStats)
                .fetchedAt(Instant.now())
                .build();
    }

    // =========================================================================
    // GitHub Response DTOs
    // =========================================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubUserResponse {
        public String login;
        public String name;
        @JsonProperty("public_repos")
        public Integer publicRepos;
        @JsonProperty("public_gists")
        public Integer publicGists;
        public Integer followers;
        public Integer following;
        public String bio;
        public String company;
        public String location;
        @JsonProperty("created_at")
        public String createdAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubRepo {
        public String name;
        @JsonProperty("stargazers_count")
        public Integer stargazersCount;
        @JsonProperty("forks_count")
        public Integer forksCount;
    }
}
