package com.devsync.coding.analytics.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.coding.analytics.dto.*;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CodingAnalyticsServiceImpl implements CodingAnalyticsService {

    private static final Set<CodingPlatform> CONTEST_PLATFORMS = Set.of(
            CodingPlatform.CODEFORCES,
            CodingPlatform.LEETCODE,
            CodingPlatform.CODECHEF,
            CodingPlatform.ATCODER
    );

    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository platformStatSnapshotRepository;
    private final CurrentUserProvider currentUserProvider;

    public CodingAnalyticsServiceImpl(PlatformAccountRepository platformAccountRepository,
                                     PlatformStatSnapshotRepository platformStatSnapshotRepository,
                                     CurrentUserProvider currentUserProvider) {
        this.platformAccountRepository = platformAccountRepository;
        this.platformStatSnapshotRepository = platformStatSnapshotRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public CodingAnalyticsOverviewResponse getOverview() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> activeAccounts = platformAccountRepository
                .findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(currentUserId);

        if (activeAccounts.isEmpty()) {
            return new CodingAnalyticsOverviewResponse(
                    0, 0, null, null, 0, 0L, null, null, Collections.emptyList()
            );
        }

        List<UUID> accountIds = activeAccounts.stream().map(PlatformAccount::getId).toList();
        long totalSnapshots = platformStatSnapshotRepository.countByAccountIds(accountIds);
        Instant firstSnapshotAt = platformStatSnapshotRepository.findEarliestSnapshotAtByAccountIds(accountIds);
        Instant lastSnapshotAt = platformStatSnapshotRepository.findLatestSnapshotAtByAccountIds(accountIds);

        int totalSolved = 0;
        int totalContests = 0;
        Double highestRating = null;
        CodingPlatform highestRatingPlatform = null;
        List<CodingAnalyticsOverviewResponse.PlatformOverviewItem> items = new ArrayList<>();

        for (PlatformAccount account : activeAccounts) {
            Optional<PlatformStatSnapshot> latestOpt = platformStatSnapshotRepository
                    .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

            Integer solved = null;
            Double rating = null;
            Long rank = null;
            Integer contests = null;

            if (latestOpt.isPresent()) {
                PlatformStatSnapshot latest = latestOpt.get();
                solved = latest.getProblemsSolved();
                if (solved != null) {
                    totalSolved += solved;
                }

                contests = latest.getContestsParticipated();
                if (contests != null) {
                    totalContests += contests;
                }

                rating = latest.getCurrentRating() != null ? latest.getCurrentRating() : latest.getMaximumRating();
                rank = latest.getGlobalRank();

                if (rating != null && (highestRating == null || rating > highestRating)) {
                    highestRating = rating;
                    highestRatingPlatform = account.getPlatform();
                }
            }

            items.add(new CodingAnalyticsOverviewResponse.PlatformOverviewItem(
                    account.getPlatform(),
                    account.getUsername(),
                    solved,
                    rating,
                    rank,
                    contests,
                    account.getLastSyncedAt()
            ));
        }

        return new CodingAnalyticsOverviewResponse(
                totalSolved,
                activeAccounts.size(),
                highestRating,
                highestRatingPlatform,
                totalContests,
                totalSnapshots,
                firstSnapshotAt,
                lastSnapshotAt,
                items
        );
    }

    @Override
    public RatingHistoryResponse getRatingHistory(CodingPlatform platform, Instant from, Instant to, String period) {
        DateRange range = resolveDateRange(from, to, period);
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> accounts = getTargetAccounts(currentUserId, platform);

        if (accounts.isEmpty()) {
            return new RatingHistoryResponse(range.periodLabel(), range.from(), range.to(), Collections.emptyList());
        }

        List<RatingHistoryResponse.PlatformRatingSeries> seriesList = new ArrayList<>();

        for (PlatformAccount account : accounts) {
            List<PlatformStatSnapshot> snapshots = platformStatSnapshotRepository
                    .findByAccountIdAndDateRangeAsc(account.getId(), range.from(), range.to());

            List<RatingHistoryResponse.RatingPoint> points = new ArrayList<>();
            for (PlatformStatSnapshot s : snapshots) {
                if (s.getCurrentRating() != null) {
                    points.add(new RatingHistoryResponse.RatingPoint(
                            s.getSnapshotAt(),
                            s.getCurrentRating(),
                            s.getGlobalRank()
                    ));
                }
            }

            Optional<PlatformStatSnapshot> latestOpt = platformStatSnapshotRepository
                    .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());
            Double currentRating = latestOpt.map(PlatformStatSnapshot::getCurrentRating).orElse(null);
            Double maximumRating = latestOpt.map(PlatformStatSnapshot::getMaximumRating).orElse(null);
            Double personalBest = platformStatSnapshotRepository.findMaxCurrentRatingByAccountId(account.getId());
            if (personalBest == null && maximumRating != null) {
                personalBest = maximumRating;
            }

            Double ratingChange = null;
            if (points.size() >= 2) {
                RatingHistoryResponse.RatingPoint firstPoint = points.getFirst();
                RatingHistoryResponse.RatingPoint lastPoint = points.getLast();
                ratingChange = Math.round((lastPoint.getRating() - firstPoint.getRating()) * 10.0) / 10.0;
            }

            seriesList.add(new RatingHistoryResponse.PlatformRatingSeries(
                    account.getPlatform(),
                    account.getUsername(),
                    currentRating,
                    maximumRating,
                    personalBest,
                    ratingChange,
                    !points.isEmpty(),
                    points
            ));
        }

        return new RatingHistoryResponse(range.periodLabel(), range.from(), range.to(), seriesList);
    }

    @Override
    public ProblemDistributionResponse getProblemDistribution(CodingPlatform platform, Instant from, Instant to, String period) {
        DateRange range = resolveDateRange(from, to, period);
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> accounts = getTargetAccounts(currentUserId, platform);

        if (accounts.isEmpty()) {
            return new ProblemDistributionResponse(
                    range.periodLabel(), range.from(), range.to(),
                    0, 0, 0, 0, 0, 0, false,
                    "No active coding platform accounts connected.",
                    Collections.emptyList()
            );
        }

        int totalPlatformReportedSolved = 0;
        int easyTotal = 0;
        int mediumTotal = 0;
        int hardTotal = 0;
        int unclassifiedTotal = 0;
        int totalDeltaInPeriod = 0;

        List<ProblemDistributionResponse.PlatformProblemBreakdown> breakdowns = new ArrayList<>();

        for (PlatformAccount account : accounts) {
            Optional<PlatformStatSnapshot> latestOpt = platformStatSnapshotRepository
                    .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

            Integer solved = 0;
            Integer easy = null;
            Integer medium = null;
            Integer hard = null;

            if (latestOpt.isPresent()) {
                PlatformStatSnapshot latest = latestOpt.get();
                solved = latest.getProblemsSolved() != null ? latest.getProblemsSolved() : 0;
                easy = latest.getEasySolved();
                medium = latest.getMediumSolved();
                hard = latest.getHardSolved();

                totalPlatformReportedSolved += solved;
                if (easy != null) easyTotal += easy;
                if (medium != null) mediumTotal += medium;
                if (hard != null) hardTotal += hard;

                int categorized = (easy != null ? easy : 0) + (medium != null ? medium : 0) + (hard != null ? hard : 0);
                if (solved > categorized) {
                    unclassifiedTotal += (solved - categorized);
                }
            }

            // Calculate historical delta in period
            List<PlatformStatSnapshot> rangeSnapshots = platformStatSnapshotRepository
                    .findByAccountIdAndDateRangeAsc(account.getId(), range.from(), range.to());

            Integer delta = null;
            if (rangeSnapshots.size() >= 2) {
                PlatformStatSnapshot firstSnap = rangeSnapshots.getFirst();
                PlatformStatSnapshot lastSnap = rangeSnapshots.getLast();
                if (firstSnap.getProblemsSolved() != null && lastSnap.getProblemsSolved() != null) {
                    delta = lastSnap.getProblemsSolved() - firstSnap.getProblemsSolved();
                    totalDeltaInPeriod += delta;
                }
            }

            breakdowns.add(new ProblemDistributionResponse.PlatformProblemBreakdown(
                    account.getPlatform(),
                    account.getUsername(),
                    solved,
                    easy,
                    medium,
                    hard,
                    0.0, // calculated below after total is known
                    delta
            ));
        }

        // Calculate percentages
        for (ProblemDistributionResponse.PlatformProblemBreakdown b : breakdowns) {
            if (totalPlatformReportedSolved > 0 && b.getTotalSolved() != null) {
                double pct = ((double) b.getTotalSolved() / totalPlatformReportedSolved) * 100.0;
                b.setPercentageOfTotal(Math.round(pct * 10.0) / 10.0);
            } else {
                b.setPercentageOfTotal(0.0);
            }
        }

        String note = "Cross-platform totals represent the sum of individual platform-reported counts. " +
                "Problem-level deduplication across platforms requires problem-specific identifier tracking.";

        return new ProblemDistributionResponse(
                range.periodLabel(),
                range.from(),
                range.to(),
                totalPlatformReportedSolved,
                easyTotal,
                mediumTotal,
                hardTotal,
                unclassifiedTotal,
                totalDeltaInPeriod,
                false,
                note,
                breakdowns
        );
    }

    @Override
    public CodingActivityResponse getActivity(Instant from, Instant to, String period) {
        DateRange range = resolveDateRange(from, to, period);
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> accounts = platformAccountRepository
                .findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(currentUserId);

        if (accounts.isEmpty()) {
            return new CodingActivityResponse(
                    range.periodLabel(), range.from(), range.to(),
                    false,
                    "Current platform public adapters capture periodic aggregate snapshots. Granular daily submission heatmaps require authenticated platform OAuth tokens or webhook event ingestion.",
                    0, 0, null, null,
                    "Automated snapshots collected every 6 hours",
                    Collections.emptyList()
            );
        }

        List<UUID> accountIds = accounts.stream().map(PlatformAccount::getId).toList();
        List<PlatformStatSnapshot> snapshots = platformStatSnapshotRepository
                .findByAccountIdsAndDateRangeAsc(accountIds, range.from(), range.to());

        Set<LocalDate> activeDays = new HashSet<>();
        Map<UUID, PlatformStatSnapshot> previousByAccount = new HashMap<>();
        List<CodingActivityResponse.SnapshotActivityRecord> timeline = new ArrayList<>();

        for (PlatformStatSnapshot s : snapshots) {
            activeDays.add(s.getSnapshotAt().atZone(ZoneOffset.UTC).toLocalDate());

            PlatformAccount acc = s.getPlatformAccount();
            PlatformStatSnapshot prev = previousByAccount.get(acc.getId());

            Integer solvedChange = null;
            Double ratingChange = null;

            if (prev != null) {
                if (s.getProblemsSolved() != null && prev.getProblemsSolved() != null) {
                    solvedChange = s.getProblemsSolved() - prev.getProblemsSolved();
                }
                if (s.getCurrentRating() != null && prev.getCurrentRating() != null) {
                    ratingChange = Math.round((s.getCurrentRating() - prev.getCurrentRating()) * 10.0) / 10.0;
                }
            }

            timeline.add(new CodingActivityResponse.SnapshotActivityRecord(
                    s.getSnapshotAt(),
                    acc.getPlatform(),
                    acc.getUsername(),
                    s.getProblemsSolved(),
                    s.getCurrentRating(),
                    solvedChange,
                    ratingChange
            ));

            previousByAccount.put(acc.getId(), s);
        }

        Instant firstAt = snapshots.isEmpty() ? null : snapshots.getFirst().getSnapshotAt();
        Instant lastAt = snapshots.isEmpty() ? null : snapshots.getLast().getSnapshotAt();

        return new CodingActivityResponse(
                range.periodLabel(),
                range.from(),
                range.to(),
                false, // Transparently reports that daily submission activity is not fabricated
                "Current platform public adapters capture periodic aggregate snapshots. Granular daily submission heatmaps require authenticated platform OAuth tokens or webhook event ingestion.",
                snapshots.size(),
                activeDays.size(),
                firstAt,
                lastAt,
                "Automated snapshots collected every 6 hours",
                timeline
        );
    }

    @Override
    public ContestPerformanceResponse getContestPerformance(CodingPlatform platform, Instant from, Instant to, String period) {
        DateRange range = resolveDateRange(from, to, period);
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> accounts = getTargetAccounts(currentUserId, platform);

        if (accounts.isEmpty()) {
            return new ContestPerformanceResponse(
                    range.periodLabel(), range.from(), range.to(), 0, false, Collections.emptyList()
            );
        }

        int totalContests = 0;
        boolean anySupported = false;
        List<ContestPerformanceResponse.PlatformContestRecord> records = new ArrayList<>();

        for (PlatformAccount account : accounts) {
            boolean isSupported = CONTEST_PLATFORMS.contains(account.getPlatform());
            if (isSupported) {
                anySupported = true;
            }

            Optional<PlatformStatSnapshot> latestOpt = platformStatSnapshotRepository
                    .findFirstByPlatformAccountIdOrderBySnapshotAtDesc(account.getId());

            Integer contests = null;
            Double currentRating = null;
            Double bestRating = null;

            if (latestOpt.isPresent()) {
                PlatformStatSnapshot latest = latestOpt.get();
                contests = latest.getContestsParticipated();
                if (contests != null && isSupported) {
                    totalContests += contests;
                }
                currentRating = latest.getCurrentRating();
            }

            List<ContestPerformanceResponse.ContestRatingPoint> contestHistory = new ArrayList<>();
            Double ratingChange = null;

            if (isSupported) {
                List<PlatformStatSnapshot> snapshots = platformStatSnapshotRepository
                        .findByAccountIdAndDateRangeAsc(account.getId(), range.from(), range.to());

                for (PlatformStatSnapshot s : snapshots) {
                    if (s.getCurrentRating() != null || s.getContestsParticipated() != null) {
                        contestHistory.add(new ContestPerformanceResponse.ContestRatingPoint(
                                s.getSnapshotAt(),
                                s.getCurrentRating(),
                                s.getContestsParticipated()
                        ));
                    }
                }

                bestRating = platformStatSnapshotRepository.findMaxCurrentRatingByAccountId(account.getId());

                if (contestHistory.size() >= 2) {
                    Double first = contestHistory.getFirst().getRating();
                    Double last = contestHistory.getLast().getRating();
                    if (first != null && last != null) {
                        ratingChange = Math.round((last - first) * 10.0) / 10.0;
                    }
                }
            }

            records.add(new ContestPerformanceResponse.PlatformContestRecord(
                    account.getPlatform(),
                    account.getUsername(),
                    isSupported,
                    contests,
                    currentRating,
                    bestRating,
                    ratingChange,
                    contestHistory
            ));
        }

        return new ContestPerformanceResponse(
                range.periodLabel(),
                range.from(),
                range.to(),
                totalContests,
                anySupported,
                records
        );
    }

    @Override
    public PerformanceInsightsResponse getInsights(CodingPlatform platform, Instant from, Instant to, String period) {
        DateRange range = resolveDateRange(from, to, period);
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<PlatformAccount> accounts = getTargetAccounts(currentUserId, platform);

        if (accounts.isEmpty()) {
            return new PerformanceInsightsResponse(
                    range.periodLabel(), range.from(), range.to(), 0, Collections.emptyList()
            );
        }

        List<PerformanceInsightsResponse.PerformanceInsightItem> items = new ArrayList<>();
        int solvedDeltaTotal = 0;
        int accountsWithSnapshots = 0;

        for (PlatformAccount account : accounts) {
            List<PlatformStatSnapshot> snapshots = platformStatSnapshotRepository
                    .findByAccountIdAndDateRangeAsc(account.getId(), range.from(), range.to());

            if (snapshots.isEmpty()) {
                items.add(new PerformanceInsightsResponse.PerformanceInsightItem(
                        UUID.randomUUID().toString(),
                        account.getPlatform(),
                        "DATA_COVERAGE",
                        "INFORMATIONAL",
                        "No Data in Selected Period",
                        "No synchronization snapshots were recorded for " + account.getPlatform() + " between the selected dates.",
                        "0 snapshots"
                ));
                continue;
            }

            accountsWithSnapshots++;

            if (snapshots.size() == 1) {
                items.add(new PerformanceInsightsResponse.PerformanceInsightItem(
                        UUID.randomUUID().toString(),
                        account.getPlatform(),
                        "DATA_COVERAGE",
                        "INFORMATIONAL",
                        "Baseline Snapshot Established",
                        "One snapshot recorded for " + account.getPlatform() + ". A second synchronization is needed to calculate delta trends.",
                        "1 snapshot"
                ));
            } else {
                // Multi-snapshot comparisons
                PlatformStatSnapshot first = snapshots.getFirst();
                PlatformStatSnapshot last = snapshots.getLast();

                // Rating progression
                if (first.getCurrentRating() != null && last.getCurrentRating() != null) {
                    double delta = Math.round((last.getCurrentRating() - first.getCurrentRating()) * 10.0) / 10.0;
                    Double pb = platformStatSnapshotRepository.findMaxCurrentRatingByAccountId(account.getId());

                    if (delta > 0) {
                        items.add(new PerformanceInsightsResponse.PerformanceInsightItem(
                                UUID.randomUUID().toString(),
                                account.getPlatform(),
                                "RATING",
                                "POSITIVE",
                                "Rating Increase",
                                String.format("Rating climbed by +%.1f points on %s (from %.1f to %.1f).",
                                        delta, account.getPlatform(), first.getCurrentRating(), last.getCurrentRating()),
                                String.format("+%.1f", delta)
                        ));
                    } else if (delta < 0) {
                        items.add(new PerformanceInsightsResponse.PerformanceInsightItem(
                                UUID.randomUUID().toString(),
                                account.getPlatform(),
                                "RATING",
                                "NEUTRAL",
                                "Rating Adjustment",
                                String.format("Rating changed by %.1f points on %s (from %.1f to %.1f).",
                                        delta, account.getPlatform(), first.getCurrentRating(), last.getCurrentRating()),
                                String.format("%.1f", delta)
                        ));
                    }

                    if (pb != null && Objects.equals(last.getCurrentRating(), pb)) {
                        items.add(new PerformanceInsightsResponse.PerformanceInsightItem(
                                UUID.randomUUID().toString(),
                                account.getPlatform(),
                                "RATING",
                                "POSITIVE",
                                "Personal Best",
                                String.format("Current rating of %.1f on %s is the personal best recorded in historical snapshots.",
                                        pb, account.getPlatform()),
                                String.format("%.1f", pb)
                        ));
                    }
                }

                // Solved change
                if (first.getProblemsSolved() != null && last.getProblemsSolved() != null) {
                    int solvedDiff = last.getProblemsSolved() - first.getProblemsSolved();
                    if (solvedDiff > 0) {
                        solvedDeltaTotal += solvedDiff;
                        items.add(new PerformanceInsightsResponse.PerformanceInsightItem(
                                UUID.randomUUID().toString(),
                                account.getPlatform(),
                                "SOLVED",
                                "POSITIVE",
                                "Problems Solved Growth",
                                String.format("Solved %d new problem(s) on %s during this period.",
                                        solvedDiff, account.getPlatform()),
                                "+" + solvedDiff
                        ));
                    }
                }

                // Contest participation
                if (first.getContestsParticipated() != null && last.getContestsParticipated() != null) {
                    int contestDiff = last.getContestsParticipated() - first.getContestsParticipated();
                    if (contestDiff > 0) {
                        items.add(new PerformanceInsightsResponse.PerformanceInsightItem(
                                UUID.randomUUID().toString(),
                                account.getPlatform(),
                                "CONTEST",
                                "POSITIVE",
                                "Contest Activity",
                                String.format("Participated in %d contest(s) on %s.",
                                        contestDiff, account.getPlatform()),
                                "+" + contestDiff
                        ));
                    }
                }
            }
        }

        if (platform == null && solvedDeltaTotal > 0) {
            items.add(new PerformanceInsightsResponse.PerformanceInsightItem(
                    UUID.randomUUID().toString(),
                    null,
                    "SOLVED",
                    "POSITIVE",
                    "Cross-Platform Growth",
                    String.format("You solved a combined total of %d problems across all connected platforms in this period.",
                            solvedDeltaTotal),
                    "+" + solvedDeltaTotal
            ));
        }

        return new PerformanceInsightsResponse(
                range.periodLabel(),
                range.from(),
                range.to(),
                items.size(),
                items
        );
    }

    private List<PlatformAccount> getTargetAccounts(UUID userId, CodingPlatform platform) {
        List<PlatformAccount> all = platformAccountRepository
                .findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(userId);
        if (platform == null) {
            return all;
        }
        return all.stream()
                .filter(a -> a.getPlatform() == platform)
                .collect(Collectors.toList());
    }

    private DateRange resolveDateRange(Instant from, Instant to, String period) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("Start date 'from' must be before or equal to end date 'to'");
        }

        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from;
        String label = period != null ? period.toLowerCase().trim() : "30d";

        if (resolvedFrom == null) {
            switch (label) {
                case "7d" -> resolvedFrom = resolvedTo.minus(7, ChronoUnit.DAYS);
                case "30d" -> resolvedFrom = resolvedTo.minus(30, ChronoUnit.DAYS);
                case "90d" -> resolvedFrom = resolvedTo.minus(90, ChronoUnit.DAYS);
                case "1y" -> resolvedFrom = resolvedTo.minus(365, ChronoUnit.DAYS);
                case "all" -> resolvedFrom = null;
                default -> throw new BadRequestException(
                        "Invalid period: '" + period + "'. Supported: 7d, 30d, 90d, 1y, all");
            }
        } else {
            label = "custom";
        }

        return new DateRange(resolvedFrom, resolvedTo, label);
    }

    private record DateRange(Instant from, Instant to, String periodLabel) {
    }
}
