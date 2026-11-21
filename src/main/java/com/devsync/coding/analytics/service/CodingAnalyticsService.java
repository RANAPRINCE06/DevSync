package com.devsync.coding.analytics.service;

import com.devsync.coding.analytics.dto.*;
import com.devsync.coding.enums.CodingPlatform;

import java.time.Instant;

public interface CodingAnalyticsService {

    CodingAnalyticsOverviewResponse getOverview();

    RatingHistoryResponse getRatingHistory(CodingPlatform platform, Instant from, Instant to, String period);

    ProblemDistributionResponse getProblemDistribution(CodingPlatform platform, Instant from, Instant to, String period);

    CodingActivityResponse getActivity(Instant from, Instant to, String period);

    ContestPerformanceResponse getContestPerformance(CodingPlatform platform, Instant from, Instant to, String period);

    PerformanceInsightsResponse getInsights(CodingPlatform platform, Instant from, Instant to, String period);
}
