package com.devsync.team.security;

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
import org.springframework.security.crypto.password.PasswordEncoder;
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
@DisplayName("CollaborativeTeamTaskSecurityTest — Team Tasks assignment, progress & permissions")
class CollaborativeTeamTaskSecurityTest {

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

    private User aliceOwner;
    private User charlieAdmin;
    private User daveMember;
    private User frankMember;
    private User eveOutsider;

    private Team teamAlpha;
    private Team teamBeta;

    private Goal teamGoalAlpha;
    private Goal alicePersonalGoal;

    private Task daveTeamTask;
    private Task frankTeamTask;
    private Task alicePersonalTask;

    private String tokenOwner;
    private String tokenAdmin;
    private String tokenDave;
    private String tokenFrank;
    private String tokenOutsider;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        goalRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();

        String encodedPassword = passwordEncoder.encode("Password123!");

        aliceOwner = userRepository.save(User.builder().name("Alice Owner").email("alice-owner-" + UUID.randomUUID() + "@devsync.io").passwordHash(encodedPassword).timezone("UTC").active(true).build());
        charlieAdmin = userRepository.save(User.builder().name("Charlie Admin").email("charlie-admin-" + UUID.randomUUID() + "@devsync.io").passwordHash(encodedPassword).timezone("UTC").active(true).build());
        daveMember = userRepository.save(User.builder().name("Dave Member").email("dave-member-" + UUID.randomUUID() + "@devsync.io").passwordHash(encodedPassword).timezone("UTC").active(true).build());
        frankMember = userRepository.save(User.builder().name("Frank Member").email("frank-member-" + UUID.randomUUID() + "@devsync.io").passwordHash(encodedPassword).timezone("UTC").active(true).build());
        eveOutsider = userRepository.save(User.builder().name("Eve Outsider").email("eve-outsider-" + UUID.randomUUID() + "@devsync.io").passwordHash(encodedPassword).timezone("UTC").active(true).build());

        teamAlpha = teamRepository.save(Team.builder().name("Alpha Squad " + UUID.randomUUID()).description("Squad Alpha").active(true).build());
        teamBeta = teamRepository.save(Team.builder().name("Beta Squad " + UUID.randomUUID()).description("Squad Beta").active(true).build());

        // Alpha memberships
        teamMemberRepository.save(TeamMember.builder().user(aliceOwner).team(teamAlpha).role(TeamRole.OWNER).active(true).build());
        teamMemberRepository.save(TeamMember.builder().user(charlieAdmin).team(teamAlpha).role(TeamRole.ADMIN).active(true).build());
        teamMemberRepository.save(TeamMember.builder().user(daveMember).team(teamAlpha).role(TeamRole.MEMBER).active(true).build());
        teamMemberRepository.save(TeamMember.builder().user(frankMember).team(teamAlpha).role(TeamRole.MEMBER).active(true).build());

        // Beta membership
        teamMemberRepository.save(TeamMember.builder().user(eveOutsider).team(teamBeta).role(TeamRole.MEMBER).active(true).build());

        // Collaborative Team Goal
        teamGoalAlpha = goalRepository.save(Goal.builder()
                .owner(aliceOwner)
                .team(teamAlpha)
                .title("Shared Microservices Roadmap")
                .description("Squad Alpha shared goal")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.HIGH)
                .progressPercentage(20)
                .active(true)
                .teamGoal(true)
                .build());

        // Alice Personal Goal
        alicePersonalGoal = goalRepository.save(Goal.builder()
                .owner(aliceOwner)
                .team(teamAlpha)
                .title("Alice Private Goal")
                .description("Private to Alice")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(2))
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.MEDIUM)
                .progressPercentage(0)
                .active(true)
                .teamGoal(false)
                .build());

        // Collaborative Team Tasks
        daveTeamTask = taskRepository.save(Task.builder()
                .goal(teamGoalAlpha)
                .assignee(daveMember)
                .team(teamAlpha)
                .title("Build Auth Token Filter")
                .description("Assigned to Dave")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(10))
                .estimatedMinutes(180)
                .actualMinutes(0)
                .active(true)
                .teamTask(true)
                .build());

        frankTeamTask = taskRepository.save(Task.builder()
                .goal(teamGoalAlpha)
                .assignee(frankMember)
                .team(teamAlpha)
                .title("Write Integration Contracts")
                .description("Assigned to Frank")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(14))
                .estimatedMinutes(120)
                .actualMinutes(0)
                .active(true)
                .teamTask(true)
                .build());

        // Alice Personal Task
        alicePersonalTask = taskRepository.save(Task.builder()
                .goal(alicePersonalGoal)
                .assignee(aliceOwner)
                .team(teamAlpha)
                .title("Alice Personal Task")
                .description("Private task")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now().plusDays(5))
                .estimatedMinutes(60)
                .actualMinutes(30)
                .active(true)
                .teamTask(false)
                .build());

        tokenOwner = jwtTokenProvider.generateToken(aliceOwner.getId(), aliceOwner.getEmail(), List.of("USER"));
        tokenAdmin = jwtTokenProvider.generateToken(charlieAdmin.getId(), charlieAdmin.getEmail(), List.of("USER"));
        tokenDave = jwtTokenProvider.generateToken(daveMember.getId(), daveMember.getEmail(), List.of("USER"));
        tokenFrank = jwtTokenProvider.generateToken(frankMember.getId(), frankMember.getEmail(), List.of("USER"));
        tokenOutsider = jwtTokenProvider.generateToken(eveOutsider.getId(), eveOutsider.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("Active team member can view another member's collaborative Team Task")
    void activeMember_CanViewTeamTask() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + frankTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenDave))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(frankTeamTask.getId().toString()))
                .andExpect(jsonPath("$.data.title").value("Write Integration Contracts"))
                .andExpect(jsonPath("$.data.teamTask").value(true));
    }

    @Test
    @DisplayName("Outsider cannot view collaborative Team Task — returns 404 (Zero Leakage)")
    void outsider_CannotViewTeamTask_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + daveTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenOutsider))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Team Owner can create and assign a collaborative Team Task")
    void owner_CanCreateAndAssignTeamTask() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(teamGoalAlpha.getId())
                .assigneeId(daveMember.getId())
                .title("New Assigned Task by Owner")
                .description("Task details")
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(7))
                .estimatedMinutes(90)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + tokenOwner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Assigned Task by Owner"))
                .andExpect(jsonPath("$.data.teamTask").value(true))
                .andExpect(jsonPath("$.data.assigneeId").value(daveMember.getId().toString()));
    }

    @Test
    @DisplayName("Team Admin can create and assign a collaborative Team Task")
    void admin_CanCreateAndAssignTeamTask() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(teamGoalAlpha.getId())
                .assigneeId(frankMember.getId())
                .title("New Assigned Task by Admin")
                .description("Task details")
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .estimatedMinutes(60)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Assigned Task by Admin"))
                .andExpect(jsonPath("$.data.teamTask").value(true));
    }

    @Test
    @DisplayName("Regular Member CANNOT create collaborative Team Task — returns 403 Forbidden")
    void member_CannotCreateTeamTask_Returns403() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(teamGoalAlpha.getId())
                .assigneeId(daveMember.getId())
                .title("Member Self-Created Team Task")
                .priority(TaskPriority.LOW)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + tokenDave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Assigning Team Task to a non-member is rejected with 400 Bad Request")
    void assignTeamTask_ToNonMember_ThrowsBadRequest() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(teamGoalAlpha.getId())
                .assigneeId(eveOutsider.getId()) // Eve is in Beta, not Alpha
                .title("Invalid Outsider Assignment")
                .priority(TaskPriority.HIGH)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + tokenOwner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Assigned member CAN update own task progress (status & actual minutes)")
    void assignedMember_CanUpdateOwnProgress() throws Exception {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Build Auth Token Filter") // Unchanged title
                .description("Assigned to Dave") // Unchanged description
                .priority(TaskPriority.HIGH) // Unchanged priority
                .dueDate(daveTeamTask.getDueDate()) // Unchanged due date
                .status(TaskStatus.IN_PROGRESS) // Updated progress status
                .actualMinutes(45) // Updated progress minutes
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + daveTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenDave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.actualMinutes").value(45));
    }

    @Test
    @DisplayName("Assigned member CANNOT modify title or priority on assigned task — returns 403 Forbidden")
    void assignedMember_CannotModifyTaskMetadata_Returns403() throws Exception {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Tampered Title by Assignee") // Changed title
                .description("Assigned to Dave")
                .priority(TaskPriority.LOW) // Changed priority
                .dueDate(daveTeamTask.getDueDate())
                .status(TaskStatus.IN_PROGRESS)
                .actualMinutes(30)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + daveTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenDave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Regular Member CANNOT update task assigned to another member — returns 403 Forbidden")
    void member_CannotUpdateTaskAssignedToOtherMember_Returns403() throws Exception {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Write Integration Contracts")
                .description("Assigned to Frank")
                .priority(TaskPriority.MEDIUM)
                .dueDate(frankTeamTask.getDueDate())
                .status(TaskStatus.IN_PROGRESS)
                .actualMinutes(60)
                .build();

        // Dave attempting to update Frank's task
        mockMvc.perform(put("/api/v1/tasks/" + frankTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenDave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Team Admin CAN update all fields on any team task")
    void admin_CanUpdateAnyTeamTask_AllFields() throws Exception {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Build Auth Token Filter (Admin Revised)")
                .description("Revised by Admin")
                .priority(TaskPriority.CRITICAL)
                .dueDate(LocalDate.now().plusDays(20))
                .status(TaskStatus.IN_PROGRESS)
                .actualMinutes(90)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + daveTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Build Auth Token Filter (Admin Revised)"))
                .andExpect(jsonPath("$.data.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.data.actualMinutes").value(90));
    }

    @Test
    @DisplayName("Regular Member CANNOT delete a collaborative Team Task — returns 403 Forbidden")
    void member_CannotDeleteTeamTask_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/" + daveTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenDave))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Team Admin CAN delete a collaborative Team Task")
    void admin_CanDeleteTeamTask() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/" + daveTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Team Member CANNOT access another user's personal task in same team — returns 404")
    void member_CannotAccessOtherUserPersonalTask_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + alicePersonalTask.getId())
                        .header("Authorization", "Bearer " + tokenDave))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Team Tasks workspace listing returns all team tasks but never other members' personal tasks")
    void teamWorkspaceListing_IsolatesPersonalTasks() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .param("teamId", teamAlpha.getId().toString())
                        .header("Authorization", "Bearer " + tokenDave))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].title", hasItems(
                        "Build Auth Token Filter",
                        "Write Integration Contracts"
                )))
                .andExpect(jsonPath("$.data.content[*].title", not(hasItem("Alice Personal Task"))));
    }

    @Test
    @DisplayName("Assigned member partial update omitting description/dueDate preserves existing metadata and updates progress")
    void assignedMember_PartialUpdate_PreservesMetadataAndUpdatesProgress() throws Exception {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Build Auth Token Filter")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.COMPLETED)
                .actualMinutes(75)
                // description and dueDate omitted (null)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + daveTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenDave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.actualMinutes").value(75))
                .andExpect(jsonPath("$.data.description").value("Assigned to Dave"))
                .andExpect(jsonPath("$.data.dueDate").value(daveTeamTask.getDueDate().toString()));
    }

    @Test
    @DisplayName("Assigned member modifying dueDate is rejected with 403 Forbidden")
    void assignedMember_ModifyingDueDate_Returns403() throws Exception {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Build Auth Token Filter")
                .description("Assigned to Dave")
                .priority(TaskPriority.HIGH)
                .dueDate(daveTeamTask.getDueDate().plusDays(5)) // Modified due date
                .status(TaskStatus.IN_PROGRESS)
                .actualMinutes(30)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + daveTeamTask.getId())
                        .header("Authorization", "Bearer " + tokenDave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

