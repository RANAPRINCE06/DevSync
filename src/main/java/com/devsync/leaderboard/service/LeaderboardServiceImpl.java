package com.devsync.leaderboard.service;

import com.devsync.achievement.entity.Achievement;
import com.devsync.achievement.repository.AchievementRepository;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.repository.GoalRepository;
import com.devsync.leaderboard.dto.LeaderboardEntryResponse;
import com.devsync.leaderboard.entity.LeaderboardPeriod;
import com.devsync.progress.entity.DailyProgress;
import com.devsync.progress.repository.DailyProgressRepository;
import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskStatus;
import com.devsync.task.repository.TaskRepository;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final GoalRepository goalRepository;
    private final TaskRepository taskRepository;
    private final AchievementRepository achievementRepository;

    public LeaderboardServiceImpl(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            DailyProgressRepository dailyProgressRepository,
            GoalRepository goalRepository,
            TaskRepository taskRepository,
            AchievementRepository achievementRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.dailyProgressRepository = dailyProgressRepository;
        this.goalRepository = goalRepository;
        this.taskRepository = taskRepository;
        this.achievementRepository = achievementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardEntryResponse> getTeamLeaderboard(UUID teamId, LeaderboardPeriod period, Pageable pageable) {
        List<LeaderboardEntryResponse> fullLeaderboard = calculateLeaderboard(teamId, period);
        return paginateList(fullLeaderboard, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaderboardEntryResponse getUserRanking(UUID teamId, UUID userId, LeaderboardPeriod period) {
        List<LeaderboardEntryResponse> fullLeaderboard = calculateLeaderboard(teamId, period);
        return fullLeaderboard.stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User not found in team leaderboard or is not an active team member"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardEntryResponse> getLeaderboardForPeriod(UUID teamId, LeaderboardPeriod period, Pageable pageable) {
        return getTeamLeaderboard(teamId, period, pageable);
    }

    private List<LeaderboardEntryResponse> calculateLeaderboard(UUID teamId, LeaderboardPeriod period) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        List<TeamMember> activeMembers = teamMemberRepository.findByTeamId(teamId).stream()
                .filter(TeamMember::isActive)
                .toList();

        if (activeMembers.isEmpty()) {
            return Collections.emptyList();
        }

        LeaderboardPeriod targetPeriod = period != null ? period : LeaderboardPeriod.ALL_TIME;

        LocalDate fromDate = null;
        LocalDate toDate = null;
        Instant fromInstant = null;
        Instant toInstant = null;

        LocalDate today = LocalDate.now();
        switch (targetPeriod) {
            case DAILY -> {
                fromDate = today;
                toDate = today;
                fromInstant = today.atStartOfDay(ZoneOffset.UTC).toInstant();
                toInstant = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            }
            case WEEKLY -> {
                fromDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                toDate = today;
                fromInstant = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                toInstant = Instant.now();
            }
            case MONTHLY -> {
                fromDate = today.withDayOfMonth(1);
                toDate = today;
                fromInstant = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                toInstant = Instant.now();
            }
            case ALL_TIME -> {
                // No date restriction
            }
        }

        // Fetch team resources
        List<DailyProgress> teamProgress = dailyProgressRepository.findAll((root, query, cb) ->
                cb.equal(root.get("team").get("id"), teamId));

        List<Goal> teamGoals = goalRepository.findByTeamId(teamId);
        List<Task> teamTasks = taskRepository.findByTeamId(teamId);

        List<LeaderboardEntryResponse> entries = new ArrayList<>();

        for (TeamMember member : activeMembers) {
            User user = member.getUser();
            UUID userId = user.getId();

            final LocalDate finalFromDate = fromDate;
            final LocalDate finalToDate = toDate;
            final Instant finalFromInstant = fromInstant;
            final Instant finalToInstant = toInstant;

            // 1. Daily progress count (+10)
            long progressCount = teamProgress.stream()
                    .filter(p -> p.getUser().getId().equals(userId))
                    .filter(p -> finalFromDate == null || (!p.getProgressDate().isBefore(finalFromDate) && !p.getProgressDate().isAfter(finalToDate)))
                    .count();

            // 2. Completed task count (+20)
            long completedTasksCount = teamTasks.stream()
                    .filter(t -> t.getAssignee().getId().equals(userId))
                    .filter(Task::isActive)
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                    .filter(t -> {
                        if (targetPeriod == LeaderboardPeriod.ALL_TIME) return true;
                        Instant completedAt = t.getCompletedAt() != null ? t.getCompletedAt() : t.getUpdatedAt();
                        return completedAt != null && !completedAt.isBefore(finalFromInstant) && !completedAt.isAfter(finalToInstant);
                    })
                    .count();

            // 3. Completed goal count (+50)
            long completedGoalsCount = teamGoals.stream()
                    .filter(g -> g.getOwner().getId().equals(userId))
                    .filter(Goal::isActive)
                    .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                    .filter(g -> {
                        if (targetPeriod == LeaderboardPeriod.ALL_TIME) return true;
                        Instant updatedAt = g.getUpdatedAt();
                        return updatedAt != null && !updatedAt.isBefore(finalFromInstant) && !updatedAt.isAfter(finalToInstant);
                    })
                    .count();

            // 4. Achievement points
            List<Achievement> userAchievements = achievementRepository.findByUserIdAndActiveTrue(userId);
            long achievementPoints = userAchievements.stream()
                    .filter(a -> {
                        if (targetPeriod == LeaderboardPeriod.ALL_TIME) return true;
                        Instant earnedAt = a.getEarnedAt();
                        return earnedAt != null && !earnedAt.isBefore(finalFromInstant) && !earnedAt.isAfter(finalToInstant);
                    })
                    .mapToLong(Achievement::getPoints)
                    .sum();

            // Deterministic score calculation
            long score = (progressCount * 10) + (completedTasksCount * 20) + (completedGoalsCount * 50) + achievementPoints;

            LeaderboardEntryResponse entry = LeaderboardEntryResponse.builder()
                    .userId(userId)
                    .userName(user.getName())
                    .teamId(teamId)
                    .teamName(team.getName())
                    .score(score)
                    .progressEntries(progressCount)
                    .completedTasks(completedTasksCount)
                    .completedGoals(completedGoalsCount)
                    .achievementPoints(achievementPoints)
                    .build();

            entries.add(entry);
        }

        // Sorting: 1. score DESC, 2. completedTasks DESC, 3. progressEntries DESC, 4. userName ASC
        entries.sort(Comparator
                .comparingLong(LeaderboardEntryResponse::getScore).reversed()
                .thenComparing(Comparator.comparingLong(LeaderboardEntryResponse::getCompletedTasks).reversed())
                .thenComparing(Comparator.comparingLong(LeaderboardEntryResponse::getProgressEntries).reversed())
                .thenComparing(LeaderboardEntryResponse::getUserName, String.CASE_INSENSITIVE_ORDER));

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return entries;
    }

    private Page<LeaderboardEntryResponse> paginateList(List<LeaderboardEntryResponse> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        List<LeaderboardEntryResponse> subList = (start > list.size()) ? Collections.emptyList() : list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }
}
