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

        // 2. Fetch Public Repositories (/users/{username}/repos?per_page=100)
        Integer totalStars = null;
        Integer totalForks = null;
        List<Map<String, Object>> repoList = new ArrayList<>();
        try {
            String reposUrl = baseUrl + "/users/" + username + "/repos?per_page=100&type=owner&sort=updated";
            GitHubRepo[] repos = client.get(reposUrl, GitHubRepo[].class, headers);
            if (repos != null) {
                int stars = 0;
                int forks = 0;
                for (GitHubRepo repo : repos) {
                    if (repo.stargazersCount != null) stars += repo.stargazersCount;
                    if (repo.forksCount != null) forks += repo.forksCount;

                    Map<String, Object> rMap = new LinkedHashMap<>();
                    rMap.put("name", repo.name);
                    rMap.put("fullName", repo.fullName != null ? repo.fullName : (username + "/" + repo.name));
                    rMap.put("htmlUrl", repo.htmlUrl != null ? repo.htmlUrl : ("https://github.com/" + username + "/" + repo.name));
                    rMap.put("description", repo.description);
                    rMap.put("language", repo.language);
                    rMap.put("starsCount", repo.stargazersCount != null ? repo.stargazersCount : 0);
                    rMap.put("forksCount", repo.forksCount != null ? repo.forksCount : 0);
                    rMap.put("isFork", Boolean.TRUE.equals(repo.fork));
                    rMap.put("defaultBranch", repo.defaultBranch != null ? repo.defaultBranch : "main");
                    rMap.put("pushedAt", repo.pushedAt);
                    rMap.put("recentCommitsCount", 0);
                    repoList.add(rMap);
                }
                totalStars = stars;
                totalForks = forks;
            }
        } catch (Exception ex) {
            log.debug("Could not calculate GitHub stars for {}: {}", username, ex.getMessage());
        }

        // 3. Fetch Public Events for Commit Activity (/users/{username}/events/public?per_page=100)
        Map<String, Integer> dailyCommitCounts = new TreeMap<>();
        try {
            String eventsUrl = baseUrl + "/users/" + username + "/events/public?per_page=100";
            GitHubEvent[] events = client.get(eventsUrl, GitHubEvent[].class, headers);
            if (events != null) {
                for (GitHubEvent evt : events) {
                    if ("PushEvent".equals(evt.type) && evt.createdAt != null) {
                        String dateStr = evt.createdAt.substring(0, 10);
                        int commitCount = (evt.payload != null && evt.payload.commits != null)
                                ? evt.payload.commits.length
                                : 1;
                        dailyCommitCounts.put(dateStr, dailyCommitCounts.getOrDefault(dateStr, 0) + commitCount);
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Could not fetch public events for {}: {}", username, ex.getMessage());
        }

        // 4. Assemble Badges
        List<Map<String, Object>> badges = new ArrayList<>();
        if (user.publicRepos != null && user.publicRepos > 0) {
            badges.add(Map.of(
                    "id", "github-repo-creator",
                    "name", "Open Source Contributor",
                    "description", "Authored " + user.publicRepos + " public repositories on GitHub",
                    "category", "GITHUB",
                    "level", user.publicRepos >= 20 ? "GOLD" : (user.publicRepos >= 5 ? "SILVER" : "BRONZE")
            ));
        }
        if (totalStars != null && totalStars > 0) {
            badges.add(Map.of(
                    "id", "github-star-collector",
                    "name", "Star Collector",
                    "description", "Earned " + totalStars + " total stars across public repositories",
                    "category", "GITHUB",
                    "level", totalStars >= 50 ? "GOLD" : (totalStars >= 10 ? "SILVER" : "BRONZE")
            ));
        }
        if (user.followers != null && user.followers >= 5) {
            badges.add(Map.of(
                    "id", "github-community-builder",
                    "name", "Community Builder",
                    "description", "Formed an open source following of " + user.followers + " developers",
                    "category", "GITHUB",
                    "level", user.followers >= 50 ? "GOLD" : (user.followers >= 20 ? "SILVER" : "BRONZE")
            ));
        }

        // 5. Assemble Additional Statistics Map
        Map<String, Object> additionalStats = new LinkedHashMap<>();
        if (user.name != null && !user.name.trim().isEmpty()) additionalStats.put("displayName", user.name.trim());
        if (user.avatarUrl != null && !user.avatarUrl.trim().isEmpty()) additionalStats.put("avatarUrl", user.avatarUrl.trim());
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
        if (!repoList.isEmpty()) additionalStats.put("repositories", repoList);
        if (!dailyCommitCounts.isEmpty()) additionalStats.put("dailyCommitCounts", dailyCommitCounts);
        if (!badges.isEmpty()) additionalStats.put("badges", badges);

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
        @JsonProperty("avatar_url")
        public String avatarUrl;
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
        @JsonProperty("full_name")
        public String fullName;
        @JsonProperty("html_url")
        public String htmlUrl;
        public String description;
        public String language;
        @JsonProperty("stargazers_count")
        public Integer stargazersCount;
        @JsonProperty("forks_count")
        public Integer forksCount;
        public Boolean fork;
        @JsonProperty("default_branch")
        public String defaultBranch;
        @JsonProperty("pushed_at")
        public String pushedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubEvent {
        public String type;
        @JsonProperty("created_at")
        public String createdAt;
        public GitHubEventPayload payload;
        public GitHubEventRepo repo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubEventPayload {
        public Map<String, Object>[] commits;
        public Integer size;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubEventRepo {
        public String name;
    }
}
