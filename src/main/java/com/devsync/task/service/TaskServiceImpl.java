package com.devsync.task.service;

import com.devsync.auth.security.CurrentUserProvider;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.goal.entity.Goal;
import com.devsync.goal.repository.GoalRepository;
import com.devsync.task.dto.CreateTaskRequest;
import com.devsync.task.dto.TaskResponse;
import com.devsync.task.dto.UpdateTaskRequest;
import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import com.devsync.task.repository.TaskRepository;
import com.devsync.task.repository.TaskSpecification;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.service.TeamAuthorizationService;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TeamAuthorizationService teamAuthorizationService;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            GoalRepository goalRepository,
            UserRepository userRepository,
            TeamMemberRepository teamMemberRepository,
            CurrentUserProvider currentUserProvider) {
        this.taskRepository = taskRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.currentUserProvider = currentUserProvider;
        this.teamAuthorizationService = new TeamAuthorizationService(teamMemberRepository, currentUserProvider);
    }

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Goal goal = goalRepository.findByIdAndOwnerId(request.getGoalId(), currentUserId)
                .orElseGet(() -> {
                    Goal g = goalRepository.findById(request.getGoalId())
                            .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + request.getGoalId()));
                    if (g.isTeamGoal()) {
                        teamAuthorizationService.requireCanManageTeam(g.getTeam().getId());
                        return g;
                    }
                    throw new ResourceNotFoundException("Goal not found with id: " + request.getGoalId());
                });

        if (!goal.isActive()) {
            throw new BadRequestException("Cannot create a task for an inactive goal");
        }

        Team team = goal.getTeam();
        if (!team.isActive()) {
            throw new ResourceNotFoundException("Team not found with id: " + team.getId());
        }

        boolean isTeamTask = goal.isTeamGoal();
        if (request.getTeamTask() != null && request.getTeamTask() != isTeamTask) {
            throw new BadRequestException("Task teamTask flag must match parent goal's teamGoal setting");
        }

        User assignee;
        if (!isTeamTask) {
            // Personal Task: Assignee must be authenticated user
            if (request.getAssigneeId() != null && !request.getAssigneeId().equals(currentUserId)) {
                throw new AccessDeniedException("Cannot assign task to another user");
            }
            assignee = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));
        } else {
            // Collaborative Team Task:
            // Caller must be OWNER or ADMIN (validated during goal resolution above)
            // Assignee must be specified and must be an ACTIVE member of this team
            UUID targetAssigneeId = request.getAssigneeId() != null ? request.getAssigneeId() : currentUserId;
            assignee = userRepository.findById(targetAssigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetAssigneeId));

            Optional<TeamMember> assigneeMemberOpt = teamMemberRepository.findByUserIdAndTeamId(assignee.getId(), team.getId());
            if (assigneeMemberOpt.isEmpty() || !assigneeMemberOpt.get().isActive()) {
                throw new BadRequestException("Assignee is not an active member of team '" + team.getName() + "'");
            }
        }

        // Active membership check
        Optional<TeamMember> memberOpt = teamMemberRepository.findByUserIdAndTeamId(assignee.getId(), team.getId());
        if (memberOpt.isEmpty() || !memberOpt.get().isActive()) {
            throw new BadRequestException("Assignee is not an active member of team '" + team.getName() + "'");
        }

        if (request.getDueDate() != null && goal.getStartDate() != null && request.getDueDate().isBefore(goal.getStartDate())) {
            throw new BadRequestException("dueDate (" + request.getDueDate() + ") cannot be before goal startDate (" + goal.getStartDate() + ")");
        }

        if (request.getEstimatedMinutes() != null && (request.getEstimatedMinutes() < 0 || request.getEstimatedMinutes() > 1440)) {
            throw new BadRequestException("estimatedMinutes must be between 0 and 1440");
        }

        Task task = Task.builder()
                .goal(goal)
                .assignee(assignee)
                .team(team)
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .estimatedMinutes(request.getEstimatedMinutes())
                .actualMinutes(0)
                .status(TaskStatus.TODO)
                .active(true)
                .completedAt(null)
                .teamTask(isTeamTask)
                .build();

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        Optional<Task> personalOpt = taskRepository.findByIdAndGoalOwnerId(id, currentUserId);
        Task task;
        if (personalOpt.isPresent()) {
            task = personalOpt.get();
            if (task.isTeamTask()) {
                teamAuthorizationService.requireActiveMember(task.getTeam().getId());
            }
        } else {
            task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
            if (task.isTeamTask()) {
                teamAuthorizationService.requireActiveMember(task.getTeam().getId());
            } else {
                throw new ResourceNotFoundException("Task not found with id: " + id);
            }
        }

        return TaskResponse.fromEntity(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        Optional<Task> personalOpt = taskRepository.findByIdAndGoalOwnerId(id, currentUserId);
        Task task;
        if (personalOpt.isPresent()) {
            task = personalOpt.get();
        } else {
            task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
            if (!task.isTeamTask()) {
                throw new ResourceNotFoundException("Task not found with id: " + id);
            }
        }

        if (!task.isTeamTask()) {
            applyTaskUpdates(task, request);
        } else {
            TeamMember callerMembership = teamAuthorizationService.requireActiveMember(task.getTeam().getId());
            if (callerMembership.getRole() == TeamRole.OWNER || callerMembership.getRole() == TeamRole.ADMIN) {
                applyTaskUpdates(task, request);
            } else {
                if (!task.getAssignee().getId().equals(currentUserId)) {
                    throw new AccessDeniedException("Regular team members cannot update tasks assigned to other members");
                }
                boolean titleChanged = request.getTitle() != null && !request.getTitle().trim().equals(task.getTitle());
                boolean descChanged = request.getDescription() != null
                        ? !request.getDescription().trim().equals(task.getDescription() != null ? task.getDescription() : "")
                        : task.getDescription() != null;
                boolean priorityChanged = request.getPriority() != null && request.getPriority() != task.getPriority();
                boolean dueDateChanged = request.getDueDate() != null
                        ? !request.getDueDate().equals(task.getDueDate())
                        : task.getDueDate() != null;

                if (titleChanged || descChanged || priorityChanged || dueDateChanged) {
                    throw new AccessDeniedException("Regular team members can only update progress (status and actual minutes) on their assigned tasks");
                }

                if (request.getActualMinutes() != null && (request.getActualMinutes() < 0 || request.getActualMinutes() > 1440)) {
                    throw new BadRequestException("actualMinutes must be between 0 and 1440");
                }
                if (request.getStatus() == TaskStatus.COMPLETED && task.getStatus() != TaskStatus.COMPLETED) {
                    task.setCompletedAt(Instant.now());
                } else if (request.getStatus() != TaskStatus.COMPLETED && task.getStatus() == TaskStatus.COMPLETED) {
                    task.setCompletedAt(null);
                }
                task.setStatus(request.getStatus());
                task.setActualMinutes(request.getActualMinutes() != null ? request.getActualMinutes() : 0);
            }
        }

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    private void applyTaskUpdates(Task task, UpdateTaskRequest request) {
        if (request.getDueDate() != null && task.getGoal().getStartDate() != null && request.getDueDate().isBefore(task.getGoal().getStartDate())) {
            throw new BadRequestException("dueDate (" + request.getDueDate() + ") cannot be before goal startDate (" + task.getGoal().getStartDate() + ")");
        }

        if (request.getActualMinutes() != null && (request.getActualMinutes() < 0 || request.getActualMinutes() > 1440)) {
            throw new BadRequestException("actualMinutes must be between 0 and 1440");
        }

        if (request.getStatus() == TaskStatus.COMPLETED && task.getStatus() != TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
        } else if (request.getStatus() != TaskStatus.COMPLETED && task.getStatus() == TaskStatus.COMPLETED) {
            task.setCompletedAt(null);
        }

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
        task.setActualMinutes(request.getActualMinutes() != null ? request.getActualMinutes() : 0);
    }

    @Override
    @Transactional
    public void deactivateTask(UUID id) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        Optional<Task> personalOpt = taskRepository.findByIdAndGoalOwnerId(id, currentUserId);
        Task task;
        if (personalOpt.isPresent()) {
            task = personalOpt.get();
            if (task.isTeamTask()) {
                teamAuthorizationService.requireCanManageTeam(task.getTeam().getId());
            }
        } else {
            task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
            if (task.isTeamTask()) {
                teamAuthorizationService.requireCanManageTeam(task.getTeam().getId());
            } else {
                throw new ResourceNotFoundException("Task not found with id: " + id);
            }
        }

        task.setActive(false);
        taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(
            UUID goalId,
            UUID assigneeId,
            UUID teamId,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Boolean active,
            Pageable pageable) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        if (teamId == null) {
            // Personal tasks view: caller must be assignee if assigneeId is passed
            if (assigneeId != null && !assigneeId.equals(currentUserId)) {
                throw new AccessDeniedException("Cannot view tasks of another user");
            }
        } else {
            // Team workspace view: caller must be active member of team
            teamMemberRepository.findByUserIdAndTeamId(currentUserId, teamId)
                    .filter(TeamMember::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        }

        if (goalId != null) {
            goalRepository.findByIdAndOwnerId(goalId, currentUserId)
                    .orElseGet(() -> {
                        Goal g = goalRepository.findById(goalId)
                                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));
                        if (g.isTeamGoal()) {
                            teamMemberRepository.findByUserIdAndTeamId(currentUserId, g.getTeam().getId())
                                    .filter(TeamMember::isActive)
                                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));
                            return g;
                        }
                        throw new ResourceNotFoundException("Goal not found with id: " + goalId);
                    });
        }

        Boolean activeFilter = active != null ? active : true;
        Specification<Task> spec = TaskSpecification.filterWithVisibility(
                currentUserId, goalId, assigneeId, teamId, status, priority, dueDate, activeFilter, null);
        return taskRepository.findAll(spec, pageable).map(TaskResponse::fromEntity);
    }
}
