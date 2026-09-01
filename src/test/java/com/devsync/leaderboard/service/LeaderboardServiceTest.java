package com.devsync.leaderboard.service;

import com.devsync.achievement.entity.Achievement;
import com.devsync.achievement.entity.AchievementType;
import com.devsync.achievement.repository.AchievementRepository;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.repository.GoalRepository;
import com.devsync.leaderboard.dto.LeaderboardEntryResponse;
import com.devsync.leaderboard.entity.LeaderboardPeriod;
import com.devsync.progress.entity.DailyProgress;
import com.devsync.progress.entity.ProgressStatus;
import com.devsync.progress.repository.DailyProgressRepository;
import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import com.devsync.task.repository.TaskRepository;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private DailyProgressRepository dailyProgressRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    private Team sampleTeam;
    private User userAlice;
    private User userBob;
    private User userCharlie;
    private TeamMember memberAlice;
    private TeamMember memberBob;
    private TeamMember memberCharlieInactive;
    private UUID teamId;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();

        sampleTeam = Team.builder()
                .id(teamId)
                .name("DevSync Core Team")
                .active(true)
                .build();

        userAlice = User.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .email("alice@devsync.com")
                .active(true)
                .build();

        userBob = User.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .email("bob@devsync.com")
                .active(true)
                .build();

        userCharlie = User.builder()
                .id(UUID.randomUUID())
                .name("Charlie")
                .email("charlie@devsync.com")
                .active(true)
                .build();

        memberAlice = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(userAlice)
                .team(sampleTeam)
                .role(TeamRole.OWNER)
                .active(true)
                .build();

        memberBob = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(userBob)
                .team(sampleTeam)
                .role(TeamRole.MEMBER)
                .active(true)
                .build();

        memberCharlieInactive = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(userCharlie)
                .team(sampleTeam)
                .role(TeamRole.MEMBER)
                .active(false)
                .build();
    }

    @Test
    @DisplayName("getTeamLeaderboard - calculates score, ranks deterministically, excludes inactive members")
    @SuppressWarnings("unchecked")
    void getTeamLeaderboard_CalculatesScoreAndRanks() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByTeamId(teamId)).thenReturn(List.of(memberAlice, memberBob, memberCharlieInactive));

        // Alice: 1 progress (+10), 1 completed task (+20), 1 completed goal (+50), 100 achievement points -> Total = 180
        DailyProgress progressAlice = DailyProgress.builder()
                .id(UUID.randomUUID())
                .user(userAlice)
                .team(sampleTeam)
                .progressDate(LocalDate.now())
                .status(ProgressStatus.COMPLETED)
                .build();

        Goal goalAlice = Goal.builder()
                .id(UUID.randomUUID())
                .owner(userAlice)
                .team(sampleTeam)
                .title("Goal Alice")
                .status(GoalStatus.COMPLETED)
                .active(true)
                .updatedAt(Instant.now())
                .build();

        Task taskAlice = Task.builder()
                .id(UUID.randomUUID())
                .goal(goalAlice)
                .assignee(userAlice)
                .team(sampleTeam)
                .title("Task Alice")
                .status(TaskStatus.COMPLETED)
                .completedAt(Instant.now())
                .active(true)
                .build();

        Achievement achievementAlice = Achievement.builder()
                .id(UUID.randomUUID())
                .user(userAlice)
                .title("Streak Master")
                .points(100)
                .type(AchievementType.STREAK)
                .earnedAt(Instant.now())
                .active(true)
                .build();

        // Bob: 2 progress (+20), 0 tasks, 0 goals, 0 achievements -> Total = 20
        DailyProgress progressBob1 = DailyProgress.builder()
                .id(UUID.randomUUID())
                .user(userBob)
                .team(sampleTeam)
                .progressDate(LocalDate.now())
                .status(ProgressStatus.COMPLETED)
                .build();
        DailyProgress progressBob2 = DailyProgress.builder()
                .id(UUID.randomUUID())
                .user(userBob)
                .team(sampleTeam)
                .progressDate(LocalDate.now())
                .status(ProgressStatus.COMPLETED)
                .build();

        when(dailyProgressRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(progressAlice, progressBob1, progressBob2));
        when(goalRepository.findByTeamId(teamId)).thenReturn(List.of(goalAlice));
        when(taskRepository.findByTeamId(teamId)).thenReturn(List.of(taskAlice));
        when(achievementRepository.findByUserIdAndActiveTrue(userAlice.getId())).thenReturn(List.of(achievementAlice));
        when(achievementRepository.findByUserIdAndActiveTrue(userBob.getId())).thenReturn(List.of());

        Pageable pageable = PageRequest.of(0, 10);
        Page<LeaderboardEntryResponse> result = leaderboardService.getTeamLeaderboard(teamId, LeaderboardPeriod.ALL_TIME, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements()); // Charlie excluded (inactive)

        LeaderboardEntryResponse rank1 = result.getContent().get(0);
        assertEquals("Alice", rank1.getUserName());
        assertEquals(1, rank1.getRank());
        assertEquals(180L, rank1.getScore());
        assertEquals(1L, rank1.getProgressEntries());
        assertEquals(1L, rank1.getCompletedTasks());
        assertEquals(1L, rank1.getCompletedGoals());
        assertEquals(100L, rank1.getAchievementPoints());

        LeaderboardEntryResponse rank2 = result.getContent().get(1);
        assertEquals("Bob", rank2.getUserName());
        assertEquals(2, rank2.getRank());
        assertEquals(20L, rank2.getScore());
    }

    @Test
    @DisplayName("getUserRanking - returns user ranking details")
    @SuppressWarnings("unchecked")
    void getUserRanking_Success() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByTeamId(teamId)).thenReturn(List.of(memberAlice, memberBob));
        when(dailyProgressRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(goalRepository.findByTeamId(teamId)).thenReturn(List.of());
        when(taskRepository.findByTeamId(teamId)).thenReturn(List.of());
        when(achievementRepository.findByUserIdAndActiveTrue(any(UUID.class))).thenReturn(List.of());

        LeaderboardEntryResponse response = leaderboardService.getUserRanking(teamId, userAlice.getId(), LeaderboardPeriod.ALL_TIME);

        assertNotNull(response);
        assertEquals("Alice", response.getUserName());
        assertEquals(userAlice.getId(), response.getUserId());
    }

    @Test
    @DisplayName("getUserRanking - non-member throws ResourceNotFoundException")
    void getUserRanking_NonMember_ThrowsException() {
        UUID nonMemberId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByTeamId(teamId)).thenReturn(List.of(memberAlice));

        assertThrows(ResourceNotFoundException.class, () ->
                leaderboardService.getUserRanking(teamId, nonMemberId, LeaderboardPeriod.ALL_TIME));
    }

    @Test
    @DisplayName("getTeamLeaderboard - team not found throws ResourceNotFoundException")
    void getTeamLeaderboard_TeamNotFound_ThrowsException() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                leaderboardService.getTeamLeaderboard(teamId, LeaderboardPeriod.DAILY, PageRequest.of(0, 10)));
    }
}
