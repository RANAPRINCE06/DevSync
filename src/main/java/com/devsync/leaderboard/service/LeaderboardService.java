package com.devsync.leaderboard.service;

import com.devsync.leaderboard.dto.LeaderboardEntryResponse;
import com.devsync.leaderboard.entity.LeaderboardPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LeaderboardService {

    Page<LeaderboardEntryResponse> getTeamLeaderboard(UUID teamId, LeaderboardPeriod period, Pageable pageable);

    LeaderboardEntryResponse getUserRanking(UUID teamId, UUID userId, LeaderboardPeriod period);

    Page<LeaderboardEntryResponse> getLeaderboardForPeriod(UUID teamId, LeaderboardPeriod period, Pageable pageable);
}
