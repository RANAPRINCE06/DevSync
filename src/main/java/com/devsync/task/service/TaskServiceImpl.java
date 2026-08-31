package com.devsync.task.service;

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
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public TaskServiceImpl(
            TaskRepository taskRepository,
            GoalRepository goalRepository,
            UserRepository userRepository,
            TeamMemberRepository teamMemberRepository) {
        this.taskRepository = taskRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Goal goal = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + request.getGoalId()));

        if (!goal.isActive()) {
            throw new BadRequestException("Cannot create a task for an inactive goal");
        }

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAssigneeId()));

        Team team = goal.getTeam();

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
                .build();

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return TaskResponse.fromEntity(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

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

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deactivateTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
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

        Boolean activeFilter = active != null ? active : true;
        Specification<Task> spec = TaskSpecification.filter(goalId, assigneeId, teamId, status, priority, dueDate, activeFilter);
        return taskRepository.findAll(spec, pageable).map(TaskResponse::fromEntity);
    }
}
