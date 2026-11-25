package com.devsync.coding.intelligence.service;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.intelligence.dto.*;

import java.util.List;
import java.util.Map;

public interface CodingPerformanceService {

    PlatformAchievementsSummaryResponse getAchievementsSummary();

    PlatformContestHistoryResponse getContestHistory(CodingPlatform platform);

    Map<CodingPlatform, PlatformContestHistoryResponse> getAllContestHistories();

    GitHubContributionResponse getGitHubContributions();

    GitHubCommitActivityResponse getGitHubCommitActivity(String period);

    List<GitHubRepositoryResponse> getGitHubRepositories();
}
