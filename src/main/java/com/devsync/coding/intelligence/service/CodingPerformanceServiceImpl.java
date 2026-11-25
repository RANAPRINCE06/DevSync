package com.devsync.coding.intelligence.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.intelligence.dto.*;
import com.devsync.coding.intelligence.entity.GitHubRepositoryRecord;
import com.devsync.coding.intelligence.entity.PlatformAchievement;
import com.devsync.coding.intelligence.entity.PlatformContestRecord;
import com.devsync.coding.intelligence.repository.GitHubRepositoryRecordRepository;
import com.devsync.coding.intelligence.repository.PlatformAchievementRepository;
import com.devsync.coding.intelligence.repository.PlatformContestRecordRepository;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class CodingPerformanceServiceImpl implements CodingPerformanceService {

    private static final Logger log = LoggerFactory.getLogger(CodingPerformanceServiceImpl.class);

    private final CurrentUserProvider currentUserProvider;
    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository platformStatSnapshotRepository;
    private final PlatformAchievementRepository platformAchievementRepository;
    private final PlatformContestRecordRepository platformContestRecordRepository;
    private final GitHubRepositoryRecordRepository gitHubRepositoryRecordRepository;
    private final ObjectMapper objectMapper;

    public CodingPerformanceServiceImpl(CurrentUserProvider currentUserProvider,
                                       PlatformAccountRepository platformAccountRepository,
                                       PlatformStatSnapshotRepository platformStatSnapshotRepository,
                                       PlatformAchievementRepository platformAchievementRepository,
                                       PlatformContestRecordRepository platformContestRecordRepository,
                                       GitHubRepositoryRecordRepository gitHubRepositoryRecordRepository,
                                       ObjectMapper objectMapper) {
        this.currentUserProvider = currentUserProvider;
        this.platformAccountRepository = platformAccountRepository;
        this.platformStatSnapshotRepository = platformStatSnapshotRepository;
        this.platformAchievementRepository = platformAchievementRepository;
        this.platformContestRecordRepository = platformContestRecordRepository;
        this.gitHubRepositoryRecordRepository = gitHubRepositoryRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public PlatformAchievementsSummaryResponse getAchievementsSummary() {
        UUID userId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> activeAccounts = platformAccountRepository.findAllByUserIdAndActiveTrue(userId);

        // Self-heal / populate achievements from latest snapshots if relational table is empty
        for (PlatformAccount account : activeAccounts) {
            populateAchievementsFromLatestSnapshot(account);
        }

        List<PlatformAchievement> achievements = platformAchievementRepository.findAllByUserId(userId);

        Map<CodingPlatform, Integer> platformCounts = new LinkedHashMap<>();
        for (CodingPlatform p : CodingPlatform.values()) {
            platformCounts.put(p, 0);
        }

        List<PlatformAchievementResponse> responseList = new ArrayList<>();
        for (PlatformAchievement a : achievements) {
            platformCounts.put(a.getPlatform(), platformCounts.getOrDefault(a.getPlatform(), 0) + 1);
            responseList.add(toAchievementResponse(a));
        }

        List<PlatformAchievementResponse> recentlyEarned = responseList.stream()
                .limit(6)
                .toList();

        return new PlatformAchievementsSummaryResponse(
                responseList.size(),
                platformCounts,
                recentlyEarned,
                responseList
        );
    }

    @Override
    @Transactional
    public PlatformContestHistoryResponse getContestHistory(CodingPlatform platform) {
        UUID userId = currentUserProvider.getCurrentUserId();
        Optional<PlatformAccount> accountOpt = platformAccountRepository
                .findAllByUserIdAndActiveTrue(userId).stream()
                .filter(a -> a.getPlatform() == platform)
                .findFirst();

        if (accountOpt.isEmpty()) {
            return new PlatformContestHistoryResponse(
                    platform,
                    platform.getDisplayName(),
                    null,
                    null,
                    0,
                    Collections.emptyList(),
                    Collections.emptyMap()
            );
        }

        PlatformAccount account = accountOpt.get();
        populateContestsFromLatestSnapshot(account);

        List<PlatformContestRecord> records = platformContestRecordRepository
                .findAllByUserIdAndPlatformOrderByDateAsc(userId, platform);

        PlatformStatSnapshot latestSnapshot = platformStatSnapshotRepository
                .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId())
                .orElse(null);

        Double currentRating = latestSnapshot != null ? latestSnapshot.getCurrentRating() : null;
        Double highestRating = latestSnapshot != null ? latestSnapshot.getMaximumRating() : null;

        List<ContestRecordResponse> contestResponses = records.stream()
                .map(this::toContestResponse)
                .toList();

        return new PlatformContestHistoryResponse(
                platform,
                platform.getDisplayName(),
                currentRating,
                highestRating,
                contestResponses.size(),
                contestResponses,
                Collections.emptyMap()
        );
    }

    @Override
    @Transactional
    public Map<CodingPlatform, PlatformContestHistoryResponse> getAllContestHistories() {
        Map<CodingPlatform, PlatformContestHistoryResponse> map = new LinkedHashMap<>();
        CodingPlatform[] contestPlatforms = { CodingPlatform.LEETCODE, CodingPlatform.CODEFORCES, CodingPlatform.ATCODER, CodingPlatform.CODECHEF };

        for (CodingPlatform platform : contestPlatforms) {
            map.put(platform, getContestHistory(platform));
        }
        return map;
    }

    @Override
    @Transactional
    public GitHubContributionResponse getGitHubContributions() {
        UUID userId = currentUserProvider.getCurrentUserId();
        Optional<PlatformAccount> gitHubAccountOpt = platformAccountRepository
                .findAllByUserIdAndActiveTrue(userId).stream()
                .filter(a -> a.getPlatform() == CodingPlatform.GITHUB)
                .findFirst();

        if (gitHubAccountOpt.isEmpty()) {
            return new GitHubContributionResponse(0, 0, 0, 0, 0, 0, Collections.emptyList());
        }

        PlatformAccount account = gitHubAccountOpt.get();
        populateGitHubTelemetryFromLatestSnapshot(account);

        PlatformStatSnapshot latestSnapshot = platformStatSnapshotRepository
                .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId())
                .orElse(null);

        Map<String, Object> additional = latestSnapshot != null && latestSnapshot.getAdditionalStats() != null
                ? latestSnapshot.getAdditionalStats()
                : Collections.emptyMap();

        Integer publicRepos = (Integer) additional.getOrDefault("publicRepos", 0);
        Integer totalStars = (Integer) additional.getOrDefault("totalStars", 0);

        // Generate or retrieve 52-week calendar points (trailing 365 days)
        List<GitHubContributionResponse.DailyContributionPoint> dailyList = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = today.minusWeeks(52).plusDays(1);

        Map<String, Integer> recordedEvents = extractMapFromAdditionalStats(additional, "dailyCommitCounts");

        int totalContributions = 0;
        int activeDays = 0;
        int currentStreak = 0;
        int longestStreak = 0;
        int runningStreak = 0;

        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            String dateStr = d.toString();
            int count = recordedEvents.getOrDefault(dateStr, 0);

            int intensity = 0;
            if (count > 0) {
                if (count <= 2) intensity = 1;
                else if (count <= 5) intensity = 2;
                else if (count <= 10) intensity = 3;
                else intensity = 4;

                totalContributions += count;
                activeDays++;
                runningStreak++;
                if (runningStreak > longestStreak) {
                    longestStreak = runningStreak;
                }
            } else {
                runningStreak = 0;
            }

            dailyList.add(new GitHubContributionResponse.DailyContributionPoint(dateStr, count, intensity));
        }

        // Current streak is the running streak ending today or yesterday
        currentStreak = runningStreak;

        return new GitHubContributionResponse(
                publicRepos,
                totalStars,
                totalContributions,
                activeDays,
                currentStreak,
                longestStreak,
                dailyList
        );
    }

    @Override
    @Transactional
    public GitHubCommitActivityResponse getGitHubCommitActivity(String period) {
        UUID userId = currentUserProvider.getCurrentUserId();
        Optional<PlatformAccount> gitHubAccountOpt = platformAccountRepository
                .findAllByUserIdAndActiveTrue(userId).stream()
                .filter(a -> a.getPlatform() == CodingPlatform.GITHUB)
                .findFirst();

        if (gitHubAccountOpt.isEmpty()) {
            return new GitHubCommitActivityResponse(0, 0, 0, 0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        PlatformAccount account = gitHubAccountOpt.get();
        populateGitHubTelemetryFromLatestSnapshot(account);

        PlatformStatSnapshot latestSnapshot = platformStatSnapshotRepository
                .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId())
                .orElse(null);

        Map<String, Object> additional = latestSnapshot != null && latestSnapshot.getAdditionalStats() != null
                ? latestSnapshot.getAdditionalStats()
                : Collections.emptyMap();

        Map<String, Integer> dailyCommitCounts = extractMapFromAdditionalStats(additional, "dailyCommitCounts");

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int days = parsePeriodToDays(period);
        LocalDate filterStart = today.minusDays(days);

        int totalCommits = 0;
        int last7Days = 0;
        int last30Days = 0;
        int last90Days = 0;

        List<GitHubCommitActivityResponse.DailyCommitPoint> dailyPoints = new ArrayList<>();
        Map<String, Integer> monthlyMap = new TreeMap<>();

        for (LocalDate d = filterStart; !d.isAfter(today); d = d.plusDays(1)) {
            String dateStr = d.toString();
            int count = dailyCommitCounts.getOrDefault(dateStr, 0);

            long diffDays = ChronoUnit.DAYS.between(d, today);
            if (diffDays <= 7) last7Days += count;
            if (diffDays <= 30) last30Days += count;
            if (diffDays <= 90) last90Days += count;
            totalCommits += count;

            dailyPoints.add(new GitHubCommitActivityResponse.DailyCommitPoint(dateStr, count));

            String monthKey = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
            monthlyMap.put(monthKey, monthlyMap.getOrDefault(monthKey, 0) + count);
        }

        List<GitHubCommitActivityResponse.MonthlyCommitPoint> monthlyPoints = monthlyMap.entrySet().stream()
                .map(e -> new GitHubCommitActivityResponse.MonthlyCommitPoint(e.getKey(), e.getValue()))
                .toList();

        // Repository commit breakdown
        List<GitHubRepositoryRecord> repos = gitHubRepositoryRecordRepository
                .findAllByPlatformAccountIdOrderByStarsCountDesc(account.getId());

        List<GitHubCommitActivityResponse.RepoCommitBreakdown> repoBreakdown = repos.stream()
                .map(r -> new GitHubCommitActivityResponse.RepoCommitBreakdown(r.getRepoName(), r.getRecentCommitsCount()))
                .toList();

        return new GitHubCommitActivityResponse(
                totalCommits,
                last7Days,
                last30Days,
                last90Days,
                dailyPoints,
                monthlyPoints,
                repoBreakdown
        );
    }

    @Override
    @Transactional
    public List<GitHubRepositoryResponse> getGitHubRepositories() {
        UUID userId = currentUserProvider.getCurrentUserId();
        Optional<PlatformAccount> gitHubAccountOpt = platformAccountRepository
                .findAllByUserIdAndActiveTrue(userId).stream()
                .filter(a -> a.getPlatform() == CodingPlatform.GITHUB)
                .findFirst();

        if (gitHubAccountOpt.isEmpty()) {
            return Collections.emptyList();
        }

        PlatformAccount account = gitHubAccountOpt.get();
        populateGitHubTelemetryFromLatestSnapshot(account);

        List<GitHubRepositoryRecord> records = gitHubRepositoryRecordRepository
                .findAllByPlatformAccountIdOrderByStarsCountDesc(account.getId());

        return records.stream()
                .map(this::toRepositoryResponse)
                .toList();
    }

    // =========================================================================
    // Snapshot-to-Relational Populate Helpers
    // =========================================================================

    private void populateAchievementsFromLatestSnapshot(PlatformAccount account) {
        Optional<PlatformStatSnapshot> snapshotOpt = platformStatSnapshotRepository
                .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

        if (snapshotOpt.isEmpty() || snapshotOpt.get().getAdditionalStats() == null) {
            return;
        }

        Map<String, Object> additional = snapshotOpt.get().getAdditionalStats();
        Object badgesObj = additional.get("badges");
        if (badgesObj instanceof List<?> rawList) {
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> map) {
                    Object idObj = map.get("id");
                    if (idObj == null) idObj = map.get("achievementId");
                    String achievementId = idObj != null ? String.valueOf(idObj) : null;

                    Object nameObj = map.get("name");
                    if (nameObj == null) nameObj = map.get("displayName");
                    String name = nameObj != null ? String.valueOf(nameObj) : null;

                    if (achievementId == null || "null".equals(achievementId) || name == null || "null".equals(name)) {
                        continue;
                    }

                    if (platformAchievementRepository.findByPlatformAccountIdAndAchievementId(account.getId(), achievementId).isEmpty()) {
                        String description = map.get("description") != null ? String.valueOf(map.get("description")) : null;
                        String imageUrl = map.get("icon") != null ? String.valueOf(map.get("icon")) : (map.get("imageUrl") != null ? String.valueOf(map.get("imageUrl")) : null);
                        String category = map.get("category") != null ? String.valueOf(map.get("category")) : "BADGE";
                        String level = map.get("level") != null ? String.valueOf(map.get("level")) : null;

                        PlatformAchievement pa = new PlatformAchievement();
                        pa.setPlatformAccount(account);
                        pa.setPlatform(account.getPlatform());
                        pa.setAchievementId(achievementId);
                        pa.setName(name);
                        pa.setDescription(description);
                        pa.setImageUrl(imageUrl);
                        pa.setCategory(category);
                        pa.setLevel(level);
                        pa.setSourceUrl(account.getProfileUrl());
                        pa.setLastVerifiedAt(Instant.now());

                        try {
                            platformAchievementRepository.save(pa);
                        } catch (Exception ex) {
                            log.debug("Duplicate achievement suppressed for {}: {}", achievementId, ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void populateContestsFromLatestSnapshot(PlatformAccount account) {
        Optional<PlatformStatSnapshot> snapshotOpt = platformStatSnapshotRepository
                .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

        if (snapshotOpt.isEmpty() || snapshotOpt.get().getAdditionalStats() == null) {
            return;
        }

        Map<String, Object> additional = snapshotOpt.get().getAdditionalStats();
        Object contestObj = additional.get("contestHistory");
        if (contestObj instanceof List<?> rawList) {
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> map) {
                    String contestId = map.get("contestId") != null ? String.valueOf(map.get("contestId")) : null;
                    Object contestNameObj = map.get("contestName");
                    String contestName = contestNameObj != null ? String.valueOf(contestNameObj) : contestId;
                    if (contestId == null || "null".equals(contestId)) {
                        continue;
                    }

                    if (platformContestRecordRepository.findByPlatformAccountIdAndContestId(account.getId(), contestId).isEmpty()) {
                        PlatformContestRecord rec = new PlatformContestRecord();
                        rec.setPlatformAccount(account);
                        rec.setPlatform(account.getPlatform());
                        rec.setContestId(contestId);
                        rec.setContestName(contestName);

                        if (map.get("contestDate") != null) {
                            try {
                                rec.setContestDate(Instant.parse(String.valueOf(map.get("contestDate"))));
                            } catch (Exception e) {
                                rec.setContestDate(Instant.now());
                            }
                        } else {
                            rec.setContestDate(Instant.now());
                        }

                        if (map.get("contestRank") != null) {
                            rec.setContestRank(((Number) map.get("contestRank")).intValue());
                        }
                        if (map.get("previousRating") != null) {
                            rec.setPreviousRating(((Number) map.get("previousRating")).doubleValue());
                        }
                        if (map.get("ratingChange") != null) {
                            rec.setRatingChange(((Number) map.get("ratingChange")).doubleValue());
                        }
                        if (map.get("newRating") != null) {
                            rec.setNewRating(((Number) map.get("newRating")).doubleValue());
                        } else {
                            rec.setNewRating(0.0);
                        }
                        if (map.get("problemsSolved") != null) {
                            rec.setProblemsSolved(((Number) map.get("problemsSolved")).intValue());
                        }

                        try {
                            platformContestRecordRepository.save(rec);
                        } catch (Exception ex) {
                            log.debug("Duplicate contest suppressed for {}: {}", contestId, ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void populateGitHubTelemetryFromLatestSnapshot(PlatformAccount account) {
        Optional<PlatformStatSnapshot> snapshotOpt = platformStatSnapshotRepository
                .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

        if (snapshotOpt.isEmpty() || snapshotOpt.get().getAdditionalStats() == null) {
            return;
        }

        Map<String, Object> additional = snapshotOpt.get().getAdditionalStats();
        Object reposObj = additional.get("repositories");
        if (reposObj instanceof List<?> rawList) {
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> map) {
                    String repoName = String.valueOf(map.get("name"));
                    if (repoName == null || "null".equals(repoName)) {
                        continue;
                    }

                    if (gitHubRepositoryRecordRepository.findByPlatformAccountIdAndRepoName(account.getId(), repoName).isEmpty()) {
                        GitHubRepositoryRecord rec = new GitHubRepositoryRecord();
                        rec.setPlatformAccount(account);
                        rec.setRepoName(repoName);
                        rec.setPrimaryLanguage(map.get("primaryLanguage") != null ? String.valueOf(map.get("primaryLanguage")) : null);
                        rec.setStarsCount(map.get("stars") != null ? ((Number) map.get("stars")).intValue() : 0);
                        rec.setForksCount(map.get("forks") != null ? ((Number) map.get("forks")).intValue() : 0);
                        rec.setRecentCommitsCount(map.get("recentCommits") != null ? ((Number) map.get("recentCommits")).intValue() : 0);
                        rec.setRepoUrl(map.get("repoUrl") != null ? String.valueOf(map.get("repoUrl")) : account.getProfileUrl());
                        rec.setDescription(map.get("description") != null ? String.valueOf(map.get("description")) : null);

                        if (map.get("lastUpdatedAt") != null) {
                            try {
                                rec.setLastUpdatedAt(Instant.parse(String.valueOf(map.get("lastUpdatedAt"))));
                            } catch (Exception e) {
                                rec.setLastUpdatedAt(Instant.now());
                            }
                        }

                        try {
                            gitHubRepositoryRecordRepository.save(rec);
                        } catch (Exception ex) {
                            log.debug("Duplicate GitHub repository record suppressed for {}: {}", repoName, ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // Mapping & Extraction Helpers
    // =========================================================================

    private PlatformAchievementResponse toAchievementResponse(PlatformAchievement a) {
        return new PlatformAchievementResponse(
                a.getId(),
                a.getPlatform(),
                a.getPlatform().getDisplayName(),
                a.getAchievementId(),
                a.getName(),
                a.getDescription(),
                a.getImageUrl(),
                a.getCategory(),
                a.getLevel(),
                a.getEarnedDate(),
                a.getSourceUrl(),
                a.getLastVerifiedAt()
        );
    }

    private ContestRecordResponse toContestResponse(PlatformContestRecord r) {
        return new ContestRecordResponse(
                r.getId(),
                r.getPlatform(),
                r.getContestId(),
                r.getContestName(),
                r.getContestDate(),
                r.getContestRank(),
                r.getParticipantsCount(),
                r.getPreviousRating(),
                r.getRatingChange(),
                r.getNewRating(),
                r.getProblemsSolved(),
                r.getPerformanceScore()
        );
    }

    private GitHubRepositoryResponse toRepositoryResponse(GitHubRepositoryRecord r) {
        return new GitHubRepositoryResponse(
                r.getId(),
                r.getRepoName(),
                r.getPrimaryLanguage(),
                r.getStarsCount(),
                r.getForksCount(),
                r.getRecentCommitsCount(),
                r.getLastUpdatedAt(),
                r.getRepoUrl(),
                r.getDescription()
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> extractMapFromAdditionalStats(Map<String, Object> additional, String key) {
        Object val = additional.get(key);
        if (val instanceof Map<?, ?> m) {
            Map<String, Integer> result = new LinkedHashMap<>();
            m.forEach((k, v) -> {
                if (k != null && v instanceof Number n) {
                    result.put(String.valueOf(k), n.intValue());
                }
            });
            return result;
        }
        return Collections.emptyMap();
    }

    private int parsePeriodToDays(String period) {
        if (period == null || period.trim().isEmpty()) return 30;
        return switch (period.toLowerCase().trim()) {
            case "7d" -> 7;
            case "30d" -> 30;
            case "90d" -> 90;
            case "6m" -> 180;
            case "1y" -> 365;
            default -> 365;
        };
    }
}
