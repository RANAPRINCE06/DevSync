package com.devsync.goal.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.goal.dto.CreateGoalRequest;
import com.devsync.goal.dto.GoalResponse;
import com.devsync.goal.dto.UpdateGoalRequest;
import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.repository.GoalRepository;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private GoalServiceImpl goalService;

    private User sampleUser;
    private Team sampleTeam;
    private TeamMember sampleMember;
    private Goal sampleGoal;
    private UUID ownerId;
    private UUID teamId;
    private UUID goalId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        goalId = UUID.randomUUID();

        sampleUser = User.builder()
                .id(ownerId)
                .name("Prince")
                .email("prince@devsync.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        sampleTeam = Team.builder()
                .id(teamId)
                .name("DevSync Core Team")
                .description("Accountability squad")
                .active(true)
                .build();

        sampleMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(sampleUser)
                .team(sampleTeam)
                .role(TeamRole.MEMBER)
                .active(true)
                .build();

        sampleGoal = Goal.builder()
                .id(goalId)
                .owner(sampleUser)
                .team(sampleTeam)
                .title("Master Spring Boot & Cloud")
                .description("Learn architecture patterns")
                .startDate(LocalDate.of(2026, 9, 1))
                .targetDate(LocalDate.of(2026, 12, 31))
                .priority(GoalPriority.HIGH)
                .status(GoalStatus.NOT_STARTED)
                .progressPercentage(0)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createGoal - success")
    void createGoal_Success() {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(ownerId)
                .teamId(teamId)
                .title("Master Spring Boot & Cloud")
                .description("Learn architecture patterns")
                .startDate(LocalDate.of(2026, 9, 1))
                .targetDate(LocalDate.of(2026, 12, 31))
                .priority(GoalPriority.HIGH)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(ownerId, teamId)).thenReturn(Optional.of(sampleMember));
        when(goalRepository.save(any(Goal.class))).thenReturn(sampleGoal);

        GoalResponse response = goalService.createGoal(request);

        assertNotNull(response);
        assertEquals(goalId, response.getId());
        assertEquals("Master Spring Boot & Cloud", response.getTitle());
        assertEquals(GoalStatus.NOT_STARTED, response.getStatus());
        assertEquals(0, response.getProgressPercentage());
        assertTrue(response.isActive());
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    @DisplayName("createGoal - owner not found throws ResourceNotFoundException")
    void createGoal_OwnerNotFound_ThrowsException() {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(ownerId)
                .teamId(teamId)
                .title("Goal")
                .priority(GoalPriority.LOW)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> goalService.createGoal(request));
        verify(goalRepository, never()).save(any(Goal.class));
    }

    @Test
    @DisplayName("createGoal - team not found throws ResourceNotFoundException")
    void createGoal_TeamNotFound_ThrowsException() {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(ownerId)
                .teamId(teamId)
                .title("Goal")
                .priority(GoalPriority.LOW)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> goalService.createGoal(request));
        verify(goalRepository, never()).save(any(Goal.class));
    }

    @Test
    @DisplayName("createGoal - owner is not active team member throws BadRequestException")
    void createGoal_InactiveMember_ThrowsException() {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(ownerId)
                .teamId(teamId)
                .title("Goal")
                .priority(GoalPriority.LOW)
                .build();

        TeamMember inactiveMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(sampleUser)
                .team(sampleTeam)
                .active(false)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(ownerId, teamId)).thenReturn(Optional.of(inactiveMember));

        assertThrows(BadRequestException.class, () -> goalService.createGoal(request));
        verify(goalRepository, never()).save(any(Goal.class));
    }

    @Test
    @DisplayName("createGoal - startDate after targetDate throws BadRequestException")
    void createGoal_InvalidDateRange_ThrowsException() {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(ownerId)
                .teamId(teamId)
                .title("Goal")
                .startDate(LocalDate.of(2026, 12, 31))
                .targetDate(LocalDate.of(2026, 9, 1))
                .priority(GoalPriority.LOW)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(ownerId, teamId)).thenReturn(Optional.of(sampleMember));

        assertThrows(BadRequestException.class, () -> goalService.createGoal(request));
        verify(goalRepository, never()).save(any(Goal.class));
    }

    @Test
    @DisplayName("getGoalById - success")
    void getGoalById_Success() {
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(sampleGoal));

        GoalResponse response = goalService.getGoalById(goalId);

        assertNotNull(response);
        assertEquals(goalId, response.getId());
        assertEquals("Prince", response.getOwnerName());
    }

    @Test
    @DisplayName("getGoalById - not found throws ResourceNotFoundException")
    void getGoalById_NotFound_ThrowsException() {
        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> goalService.getGoalById(goalId));
    }

    @Test
    @DisplayName("updateGoal - success with auto-completion when progress reaches 100")
    void updateGoal_AutoCompletesWhenProgress100() {
        UpdateGoalRequest request = UpdateGoalRequest.builder()
                .title("Master Spring Boot & Cloud (Completed)")
                .description("All tasks done")
                .priority(GoalPriority.CRITICAL)
                .progressPercentage(100)
                .status(GoalStatus.IN_PROGRESS) // Even if sent as IN_PROGRESS, 100% sets to COMPLETED
                .build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(sampleGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(sampleGoal);

        GoalResponse response = goalService.updateGoal(goalId, request);

        assertNotNull(response);
        assertEquals(GoalStatus.COMPLETED, sampleGoal.getStatus());
        assertEquals(100, sampleGoal.getProgressPercentage());
        verify(goalRepository).save(sampleGoal);
    }

    @Test
    @DisplayName("deactivateGoal - soft deletes goal by setting active=false")
    void deactivateGoal_SoftDeletes() {
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(sampleGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(sampleGoal);

        goalService.deactivateGoal(goalId);

        assertFalse(sampleGoal.isActive());
        verify(goalRepository).save(sampleGoal);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getGoals - paginated list excludes inactive by default")
    void getGoals_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Goal> page = new PageImpl<>(List.of(sampleGoal));

        when(goalRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<GoalResponse> result = goalService.getGoals(ownerId, teamId, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Master Spring Boot & Cloud", result.getContent().get(0).getTitle());
    }
}
