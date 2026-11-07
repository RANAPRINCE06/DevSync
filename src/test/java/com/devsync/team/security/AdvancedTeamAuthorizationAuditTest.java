package com.devsync.team.security;

import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.repository.GoalRepository;
import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import com.devsync.task.repository.TaskRepository;
import com.devsync.team.dto.UpdateTeamMemberRoleRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AdvancedTeamAuthorizationAuditTest — Revocation, Soft-delete, Ownership transfer & Role integrity")
class AdvancedTeamAuthorizationAuditTest {

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
    private User bobAdmin;
    private User charlieMember;

    private Team team;

    private Goal teamGoal;
    private Goal charliePersonalGoal;
    private Task teamTask;
    private Task charliePersonalTask;

    private String tokenOwner;
    private String tokenAdmin;
    private String tokenMember;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        goalRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();

        String encodedPassword = passwordEncoder.encode("Password123!");

        aliceOwner = userRepository.save(User.builder().name("Alice Owner").email("alice-" + UUID.randomUUID() + "@devsync.io").passwordHash(encodedPassword).timezone("UTC").active(true).build());
        bobAdmin = userRepository.save(User.builder().name("Bob Admin").email("bob-" + UUID.randomUUID() + "@devsync.io").passwordHash(encodedPassword).timezone("UTC").active(true).build());
        charlieMember = userRepository.save(User.builder().name("Charlie Member").email("charlie-" + UUID.randomUUID() + "@devsync.io").passwordHash(encodedPassword).timezone("UTC").active(true).build());

        team = teamRepository.save(Team.builder().name("Audit Squad " + UUID.randomUUID()).description("Audit test team").active(true).build());

        teamMemberRepository.save(TeamMember.builder().user(aliceOwner).team(team).role(TeamRole.OWNER).active(true).build());
        teamMemberRepository.save(TeamMember.builder().user(bobAdmin).team(team).role(TeamRole.ADMIN).active(true).build());
        teamMemberRepository.save(TeamMember.builder().user(charlieMember).team(team).role(TeamRole.MEMBER).active(true).build());

        teamGoal = goalRepository.save(Goal.builder()
                .owner(aliceOwner)
                .team(team)
                .title("Shared Audit Roadmap")
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.HIGH)
                .progressPercentage(10)
                .active(true)
                .teamGoal(true)
                .build());

        charliePersonalGoal = goalRepository.save(Goal.builder()
                .owner(charlieMember)
                .team(team)
                .title("Charlie Personal Goal")
                .status(GoalStatus.NOT_STARTED)
                .priority(GoalPriority.MEDIUM)
                .progressPercentage(0)
                .active(true)
                .teamGoal(false)
                .build());

        teamTask = taskRepository.save(Task.builder()
                .goal(teamGoal)
                .assignee(charlieMember)
                .team(team)
                .title("Audit Logging Implementation")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .active(true)
                .teamTask(true)
                .build());

        charliePersonalTask = taskRepository.save(Task.builder()
                .goal(charliePersonalGoal)
                .assignee(charlieMember)
                .team(team)
                .title("Charlie Personal Study Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .active(true)
                .teamTask(false)
                .build());

        tokenOwner = jwtTokenProvider.generateToken(aliceOwner.getId(), aliceOwner.getEmail(), List.of("USER"));
        tokenAdmin = jwtTokenProvider.generateToken(bobAdmin.getId(), bobAdmin.getEmail(), List.of("USER"));
        tokenMember = jwtTokenProvider.generateToken(charlieMember.getId(), charlieMember.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("Revocation: Removed member immediately loses access to team resources via authoritative DB check")
    void memberRevocation_ImmediatelyLosesTeamAccess() throws Exception {
        // Prior to removal: member can view team goal and team task
        mockMvc.perform(get("/api/v1/goals/" + teamGoal.getId()).header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/tasks/" + teamTask.getId()).header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isOk());

        // Admin removes Charlie from team
        mockMvc.perform(delete("/api/v1/teams/" + team.getId() + "/members/" + charlieMember.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        // Charlie tries to view team resources using their existing JWT -> must return 404 Not Found
        mockMvc.perform(get("/api/v1/goals/" + teamGoal.getId()).header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/tasks/" + teamTask.getId()).header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isNotFound());

        // But Charlie retains full access to their own personal resources!
        mockMvc.perform(get("/api/v1/goals/" + charliePersonalGoal.getId()).header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Charlie Personal Goal"));
        mockMvc.perform(get("/api/v1/tasks/" + charliePersonalTask.getId()).header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Charlie Personal Study Task"));
    }

    @Test
    @DisplayName("Team Deletion: Only OWNER can soft-delete team; Admin and Member cannot")
    void teamDeletion_RestrictedToOwner() throws Exception {
        // Regular member cannot delete team -> 403
        mockMvc.perform(delete("/api/v1/teams/" + team.getId()).header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isForbidden());

        // Admin cannot delete team -> 403
        mockMvc.perform(delete("/api/v1/teams/" + team.getId()).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isForbidden());

        // Owner can delete team -> 200
        mockMvc.perform(delete("/api/v1/teams/" + team.getId()).header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isOk());

        // Once team is soft-deleted, team resource lookups must return 404
        mockMvc.perform(get("/api/v1/teams/" + team.getId()).header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/goals/" + teamGoal.getId()).header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/tasks/" + teamTask.getId()).header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Ownership Transfer: Promotes new OWNER, demotes former OWNER to ADMIN, preserving single-owner invariant")
    void ownershipTransfer_MaintainsSingleOwnerInvariant() throws Exception {
        UpdateTeamMemberRoleRequest request = new UpdateTeamMemberRoleRequest(TeamRole.OWNER);

        // Alice transfers ownership to Bob
        mockMvc.perform(put("/api/v1/teams/" + team.getId() + "/members/" + bobAdmin.getId() + "/role")
                        .header("Authorization", "Bearer " + tokenOwner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("OWNER"));

        // Verify in database: Bob is now OWNER, Alice is demoted to ADMIN
        TeamMember bobRecord = teamMemberRepository.findByUserIdAndTeamId(bobAdmin.getId(), team.getId()).orElseThrow();
        assertEquals(TeamRole.OWNER, bobRecord.getRole());

        TeamMember aliceRecord = teamMemberRepository.findByUserIdAndTeamId(aliceOwner.getId(), team.getId()).orElseThrow();
        assertEquals(TeamRole.ADMIN, aliceRecord.getRole());

        // Former owner Alice (now ADMIN) cannot delete the team -> 403
        mockMvc.perform(delete("/api/v1/teams/" + team.getId()).header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isForbidden());

        // New owner Bob can manage team ownership and delete team
        mockMvc.perform(delete("/api/v1/teams/" + team.getId()).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Role Changes: Owner demoting themselves without transferring is rejected with 400 Bad Request")
    void ownerSelfDemotion_WithoutTransfer_Returns400() throws Exception {
        UpdateTeamMemberRoleRequest request = new UpdateTeamMemberRoleRequest(TeamRole.ADMIN);

        mockMvc.perform(put("/api/v1/teams/" + team.getId() + "/members/" + aliceOwner.getId() + "/role")
                        .header("Authorization", "Bearer " + tokenOwner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Role Changes: Non-owner (Admin or Member) CANNOT change member roles — returns 403")
    void nonOwner_CannotChangeMemberRole_Returns403() throws Exception {
        UpdateTeamMemberRoleRequest request = new UpdateTeamMemberRoleRequest(TeamRole.ADMIN);

        // Admin cannot change roles
        mockMvc.perform(put("/api/v1/teams/" + team.getId() + "/members/" + charlieMember.getId() + "/role")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Member cannot promote themselves
        mockMvc.perform(put("/api/v1/teams/" + team.getId() + "/members/" + charlieMember.getId() + "/role")
                        .header("Authorization", "Bearer " + tokenMember)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

