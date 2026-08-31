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
import com.devsync.goal.repository.GoalSpecification;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public GoalServiceImpl(
            GoalRepository goalRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getOwnerId()));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.getTeamId()));

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
                .build();

        Goal saved = goalRepository.save(goal);
        return GoalResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(UUID id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
        return GoalResponse.fromEntity(goal);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(UUID id, UpdateGoalRequest request) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

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
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
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

        Boolean activeFilter = active != null ? active : true;
        Specification<Goal> spec = GoalSpecification.filter(ownerId, teamId, status, priority, activeFilter, startDate, targetDate);
        return goalRepository.findAll(spec, pageable).map(GoalResponse::fromEntity);
    }
}
