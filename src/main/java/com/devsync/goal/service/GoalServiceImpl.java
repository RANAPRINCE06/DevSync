package com.devsync.goal.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.goal.dto.CreateGoalRequest;
import com.devsync.goal.dto.GoalResponse;
import com.devsync.goal.dto.UpdateGoalRequest;
import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.repository.GoalRepository;
import com.devsync.goal.repository.GoalSpecification;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.team.service.TeamAuthorizationService;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TeamAuthorizationService teamAuthorizationService;

    public GoalServiceImpl(
            GoalRepository goalRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            CurrentUserProvider currentUserProvider) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.currentUserProvider = currentUserProvider;
        this.teamAuthorizationService = new TeamAuthorizationService(teamMemberRepository, currentUserProvider);
    }

    @Override
    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        boolean isTeamGoal = Boolean.TRUE.equals(request.getTeamGoal());

        if (isTeamGoal) {
            // Team Goal: Only team OWNER or ADMIN can create team goals
            teamAuthorizationService.requireCanManageTeam(request.getTeamId());
        } else {
            // Personal Goal: Owner must match authenticated caller
            if (request.getOwnerId() != null && !request.getOwnerId().equals(currentUserId)) {
                throw new AccessDeniedException("Cannot create a goal for another user");
            }
        }

        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.getTeamId()));

        if (!team.isActive()) {
            throw new ResourceNotFoundException("Team not found with id: " + request.getTeamId());
        }

        Optional<TeamMember> memberOpt = teamMemberRepository.findByUserIdAndTeamId(owner.getId(), team.getId());
        if (memberOpt.isEmpty() || !memberOpt.get().isActive()) {
            throw new BadRequestException("User is not an active member of team '" + team.getName() + "'");
        }

        if (request.getStartDate() != null && request.getTargetDate() != null && request.getStartDate().isAfter(request.getTargetDate())) {
            throw new BadRequestException("startDate (" + request.getStartDate() + ") cannot be after targetDate (" + request.getTargetDate() + ")");
        }

        Goal goal = Goal.builder()
                .owner(owner)
                .team(team)
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .status(GoalStatus.NOT_STARTED)
                .priority(request.getPriority())
                .progressPercentage(0)
                .active(true)
                .teamGoal(isTeamGoal)
                .build();

        Goal saved = goalRepository.save(goal);
        return GoalResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(UUID id) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        Optional<Goal> personalOpt = goalRepository.findByIdAndOwnerId(id, currentUserId);
        Goal goal;
        if (personalOpt.isPresent()) {
            goal = personalOpt.get();
            if (goal.isTeamGoal()) {
                teamAuthorizationService.requireActiveMember(goal.getTeam().getId());
            }
        } else {
            goal = goalRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
            if (goal.isTeamGoal()) {
                teamAuthorizationService.requireActiveMember(goal.getTeam().getId());
            } else {
                throw new ResourceNotFoundException("Goal not found with id: " + id);
            }
        }

        return GoalResponse.fromEntity(goal);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(UUID id, UpdateGoalRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        Optional<Goal> personalOpt = goalRepository.findByIdAndOwnerId(id, currentUserId);
        Goal goal;
        if (personalOpt.isPresent()) {
            goal = personalOpt.get();
            if (goal.isTeamGoal()) {
                teamAuthorizationService.requireCanManageTeam(goal.getTeam().getId());
            }
        } else {
            goal = goalRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
            if (goal.isTeamGoal()) {
                teamAuthorizationService.requireCanManageTeam(goal.getTeam().getId());
            } else {
                throw new ResourceNotFoundException("Goal not found with id: " + id);
            }
        }

        if (request.getStartDate() != null && request.getTargetDate() != null && request.getStartDate().isAfter(request.getTargetDate())) {
            throw new BadRequestException("startDate (" + request.getStartDate() + ") cannot be after targetDate (" + request.getTargetDate() + ")");
        }

        if (request.getProgressPercentage() != null && (request.getProgressPercentage() < 0 || request.getProgressPercentage() > 100)) {
            throw new BadRequestException("progressPercentage must be between 0 and 100");
        }

        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        goal.setStartDate(request.getStartDate());
        goal.setTargetDate(request.getTargetDate());
        goal.setPriority(request.getPriority());
        goal.setProgressPercentage(request.getProgressPercentage());

        if (request.getProgressPercentage() != null && request.getProgressPercentage() == 100) {
            goal.setStatus(GoalStatus.COMPLETED);
        } else {
            goal.setStatus(request.getStatus());
        }

        Goal saved = goalRepository.save(goal);
        return GoalResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deactivateGoal(UUID id) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        Optional<Goal> personalOpt = goalRepository.findByIdAndOwnerId(id, currentUserId);
        Goal goal;
        if (personalOpt.isPresent()) {
            goal = personalOpt.get();
            if (goal.isTeamGoal()) {
                teamAuthorizationService.requireCanManageTeam(goal.getTeam().getId());
            }
        } else {
            goal = goalRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
            if (goal.isTeamGoal()) {
                teamAuthorizationService.requireCanManageTeam(goal.getTeam().getId());
            } else {
                throw new ResourceNotFoundException("Goal not found with id: " + id);
            }
        }

        goal.setActive(false);
        goalRepository.save(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GoalResponse> getGoals(
            UUID ownerId,
            UUID teamId,
            GoalStatus status,
            GoalPriority priority,
            Boolean active,
            LocalDate startDate,
            LocalDate targetDate,
            Pageable pageable) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();
        if (ownerId != null && !ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("Cannot access goals of another user");
        }

        if (teamId != null) {
            teamMemberRepository.findByUserIdAndTeamId(currentUserId, teamId)
                    .filter(TeamMember::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        }

        Boolean activeFilter = active != null ? active : true;
        Specification<Goal> spec = GoalSpecification.filterWithVisibility(
                currentUserId, teamId, status, priority, activeFilter, startDate, targetDate, null);
        return goalRepository.findAll(spec, pageable).map(GoalResponse::fromEntity);
    }
}
