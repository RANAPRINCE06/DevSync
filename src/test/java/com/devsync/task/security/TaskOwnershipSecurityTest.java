package com.devsync.task.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.repository.GoalRepository;
import com.devsync.task.dto.CreateTaskRequest;
import com.devsync.task.dto.UpdateTaskRequest;
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
import com.devsync.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TaskOwnershipSecurityTest — Object-level authorization & IDOR protection")
class TaskOwnershipSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private Team team;
    private Goal goalA;
    private Goal goalB;
    private Task taskA;
    private Task taskB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        goalRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();

        String encodedPassword = passwordEncoder.encode("Password123!");

        userA = userRepository.save(User.builder()
                .name("Alice Architect")
                .email("alice-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        userB = userRepository.save(User.builder()
                .name("Bob Builder")
                .email("bob-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        team = teamRepository.save(Team.builder()
                .name("Engineering Squad " + UUID.randomUUID())
                .description("Task security team")
                .active(true)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .user(userA)
                .team(team)
                .role(TeamRole.MEMBER)
                .active(true)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .user(userB)
                .team(team)
                .role(TeamRole.MEMBER)
                .active(true)
                .build());

        goalA = goalRepository.save(Goal.builder()
                .owner(userA)
                .team(team)
                .title("Alice Goal")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.HIGH)
                .progressPercentage(10)
                .active(true)
                .build());

        goalB = goalRepository.save(Goal.builder()
                .owner(userB)
                .team(team)
                .title("Bob Goal")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.MEDIUM)
                .progressPercentage(0)
                .active(true)
                .build());

        taskA = taskRepository.save(Task.builder()
                .goal(goalA)
                .assignee(userA)
                .team(team)
                .title("Alice's Secret Task")
                .description("Private task description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusWeeks(1))
                .estimatedMinutes(120)
                .actualMinutes(0)
                .active(true)
                .build());

        taskB = taskRepository.save(Task.builder()
                .goal(goalB)
                .assignee(userB)
                .team(team)
                .title("Bob's Task")
                .description("Bob's task description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusWeeks(2))
                .estimatedMinutes(60)
                .actualMinutes(0)
                .active(true)
                .build());

        tokenA = jwtTokenProvider.generateToken(userA.getId(), userA.getEmail(), List.of("USER"));
        tokenB = jwtTokenProvider.generateToken(userB.getId(), userB.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("User A can list their own tasks, excluding User B's tasks")
    void userA_CanListOwnTasks() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(taskA.getId().toString()))
                .andExpect(jsonPath("$.data.content[0].title").value("Alice's Secret Task"));
    }

    @Test
    @DisplayName("User B cannot retrieve User A's task by ID — returns 404 Not Found")
    void userB_CannotRetrieveUserATask() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + taskA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Task not found with id: " + taskA.getId())));
    }

    @Test
    @DisplayName("User B cannot update User A's task — returns 404 Not Found")
    void userB_CannotUpdateUserATask() throws Exception {
        UpdateTaskRequest updateRequest = UpdateTaskRequest.builder()
                .title("Maliciously Modified Task")
                .description("Hacked description")
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now().plusWeeks(1))
                .status(TaskStatus.IN_PROGRESS)
                .actualMinutes(30)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + taskA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User B cannot delete/deactivate User A's task — returns 404 Not Found")
    void userB_CannotDeleteUserATask() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/" + taskA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User B cannot change User A's task status — returns 404 Not Found")
    void userB_CannotChangeUserATaskStatus() throws Exception {
        UpdateTaskRequest updateRequest = UpdateTaskRequest.builder()
                .title("Alice's Secret Task")
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusWeeks(1))
                .status(TaskStatus.COMPLETED)
                .actualMinutes(120)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + taskA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User A cannot assign a task to User B's goal — returns 404 Not Found")
    void userA_CannotAssignTaskToUserBGoal() throws Exception {
        CreateTaskRequest createRequest = CreateTaskRequest.builder()
                .goalId(goalB.getId()) // Bob's goal
                .assigneeId(userA.getId())
                .title("Task assigned to Bob's Goal")
                .priority(TaskPriority.MEDIUM)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User A cannot change task ownership through request data — returns 403 Forbidden")
    void userA_CannotAssignTaskToAnotherUser() throws Exception {
        CreateTaskRequest createRequest = CreateTaskRequest.builder()
                .goalId(goalA.getId())
                .assigneeId(userB.getId()) // attempting to assign to Bob
                .title("Task forced onto Bob")
                .priority(TaskPriority.MEDIUM)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User A cannot query tasks of User B via assigneeId — returns 403 Forbidden")
    void userA_CannotQueryTasksOfUserB() throws Exception {
        mockMvc.perform(get("/api/v1/tasks?assigneeId=" + userB.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User A cannot query tasks of User B's goal via goalId — returns 404 Not Found")
    void userA_CannotQueryTasksOfUserBGoal() throws Exception {
        mockMvc.perform(get("/api/v1/tasks?goalId=" + goalB.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
