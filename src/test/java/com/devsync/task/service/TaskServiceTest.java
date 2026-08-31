package com.devsync.task.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.repository.GoalRepository;
import com.devsync.task.dto.CreateTaskRequest;
import com.devsync.task.dto.TaskResponse;
import com.devsync.task.dto.UpdateTaskRequest;
import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import com.devsync.task.repository.TaskRepository;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.repository.TeamMemberRepository;
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
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User sampleAssignee;
    private Team sampleTeam;
    private TeamMember sampleMember;
    private Goal sampleGoal;
    private Task sampleTask;
    private UUID goalId;
    private UUID assigneeId;
    private UUID teamId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        goalId = UUID.randomUUID();
        assigneeId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        sampleAssignee = User.builder()
                .id(assigneeId)
                .name("Prince")
                .email("prince@devsync.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        sampleTeam = Team.builder()
                .id(teamId)
                .name("DevSync Team")
                .description("Team description")
                .active(true)
                .build();

        sampleMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(sampleAssignee)
                .team(sampleTeam)
                .role(TeamRole.MEMBER)
                .active(true)
                .build();

        sampleGoal = Goal.builder()
                .id(goalId)
                .owner(sampleAssignee)
                .team(sampleTeam)
                .title("Master Spring Boot")
                .startDate(LocalDate.of(2026, 9, 1))
                .targetDate(LocalDate.of(2026, 12, 31))
                .priority(GoalPriority.HIGH)
                .status(GoalStatus.IN_PROGRESS)
                .active(true)
                .build();

        sampleTask = Task.builder()
                .id(taskId)
                .goal(sampleGoal)
                .assignee(sampleAssignee)
                .team(sampleTeam)
                .title("Implement REST APIs")
                .description("Build CRUD endpoints")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.of(2026, 9, 15))
                .estimatedMinutes(120)
                .actualMinutes(0)
                .active(true)
                .completedAt(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createTask - success with derived team")
    void createTask_Success() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(goalId)
                .assigneeId(assigneeId)
                .title("Implement REST APIs")
                .description("Build CRUD endpoints")
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.of(2026, 9, 15))
                .estimatedMinutes(120)
                .build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(sampleGoal));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(sampleAssignee));
        when(teamMemberRepository.findByUserIdAndTeamId(assigneeId, teamId)).thenReturn(Optional.of(sampleMember));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals("Implement REST APIs", response.getTitle());
        assertEquals(goalId, response.getGoalId());
        assertEquals(teamId, response.getTeamId());
        assertEquals(TaskStatus.TODO, response.getStatus());
        assertNull(response.getCompletedAt());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask - goal not found throws ResourceNotFoundException")
    void createTask_GoalNotFound_ThrowsException() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(goalId)
                .assigneeId(assigneeId)
                .title("Task")
                .priority(TaskPriority.LOW)
                .build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask - inactive goal throws BadRequestException")
    void createTask_InactiveGoal_ThrowsException() {
        sampleGoal.setActive(false);
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(goalId)
                .assigneeId(assigneeId)
                .title("Task")
                .priority(TaskPriority.LOW)
                .build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(sampleGoal));

        assertThrows(BadRequestException.class, () -> taskService.createTask(request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask - assignee not active member in Goal's team throws BadRequestException")
    void createTask_AssigneeNotMember_ThrowsException() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(goalId)
                .assigneeId(assigneeId)
                .title("Task")
                .priority(TaskPriority.LOW)
                .build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(sampleGoal));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(sampleAssignee));
        when(teamMemberRepository.findByUserIdAndTeamId(assigneeId, teamId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> taskService.createTask(request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask - dueDate before goal startDate throws BadRequestException")
    void createTask_DueDateBeforeGoalStartDate_ThrowsException() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(goalId)
                .assigneeId(assigneeId)
                .title("Task")
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.of(2026, 8, 15)) // Goal start is 2026-09-01
                .build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(sampleGoal));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(sampleAssignee));
        when(teamMemberRepository.findByUserIdAndTeamId(assigneeId, teamId)).thenReturn(Optional.of(sampleMember));

        assertThrows(BadRequestException.class, () -> taskService.createTask(request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask - transition to COMPLETED sets completedAt")
    void updateTask_TransitionToCompleted_SetsCompletedAt() {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Implement REST APIs (Done)")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.COMPLETED)
                .actualMinutes(90)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.updateTask(taskId, request);

        assertNotNull(response);
        assertEquals(TaskStatus.COMPLETED, sampleTask.getStatus());
        assertNotNull(sampleTask.getCompletedAt());
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("updateTask - reopening completed task clears completedAt")
    void updateTask_ReopeningTask_ClearsCompletedAt() {
        sampleTask.setStatus(TaskStatus.COMPLETED);
        sampleTask.setCompletedAt(Instant.now());

        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Implement REST APIs (Reopened)")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .actualMinutes(90)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.updateTask(taskId, request);

        assertNotNull(response);
        assertEquals(TaskStatus.IN_PROGRESS, sampleTask.getStatus());
        assertNull(sampleTask.getCompletedAt());
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("deactivateTask - soft deletes task by setting active=false")
    void deactivateTask_SoftDeletes() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        taskService.deactivateTask(taskId);

        assertFalse(sampleTask.isActive());
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getTasks - paginated list excludes inactive by default")
    void getTasks_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> page = new PageImpl<>(List.of(sampleTask));

        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<TaskResponse> result = taskService.getTasks(goalId, assigneeId, teamId, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Implement REST APIs", result.getContent().get(0).getTitle());
    }
}
