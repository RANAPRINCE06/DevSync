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
import com.devsync.team.dto.UpdateTeamRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CrossTeamIsolationSecurityTest — Complete cross-team boundaries & zero information leakage")
class CrossTeamIsolationSecurityTest {

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

    private User aliceAlphaOwner;
    private User bobBetaOwner;
    private Team teamAlpha;
    private Team teamBeta;
    private Goal goalBeta;
    private Task taskBeta;
    private String tokenAlice;
    private String tokenBob;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        goalRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();

        String encodedPassword = passwordEncoder.encode("Password123!");

        aliceAlphaOwner = userRepository.save(User.builder()
                .name("Alice Alpha")
                .email("alice-alpha-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        bobBetaOwner = userRepository.save(User.builder()
                .name("Bob Beta")
                .email("bob-beta-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        teamAlpha = teamRepository.save(Team.builder()
                .name("Alpha Squad " + UUID.randomUUID())
                .description("Team Alpha workspace")
                .active(true)
                .build());

        teamBeta = teamRepository.save(Team.builder()
                .name("Beta Squad " + UUID.randomUUID())
                .description("Team Beta workspace")
                .active(true)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .user(aliceAlphaOwner)
                .team(teamAlpha)
                .role(TeamRole.OWNER)
                .active(true)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .user(bobBetaOwner)
                .team(teamBeta)
                .role(TeamRole.OWNER)
                .active(true)
                .build());

        goalBeta = goalRepository.save(Goal.builder()
                .owner(bobBetaOwner)
                .team(teamBeta)
                .title("Beta Confidential Goal")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.HIGH)
                .progressPercentage(10)
                .active(true)
                .build());

        taskBeta = taskRepository.save(Task.builder()
                .goal(goalBeta)
                .assignee(bobBetaOwner)
                .team(teamBeta)
                .title("Beta Confidential Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusWeeks(1))
                .estimatedMinutes(60)
                .actualMinutes(0)
                .active(true)
                .build());

        tokenAlice = jwtTokenProvider.generateToken(aliceAlphaOwner.getId(), aliceAlphaOwner.getEmail(), List.of("USER"));
        tokenBob = jwtTokenProvider.generateToken(bobBetaOwner.getId(), bobBetaOwner.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("Team Alpha owner cannot access Team Beta workspace — returns 404")
    void alice_CannotAccessTeamBetaWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/teams/" + teamBeta.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner cannot retrieve Team Beta member list — returns 404")
    void alice_CannotRetrieveTeamBetaMembers() throws Exception {
        mockMvc.perform(get("/api/v1/teams/" + teamBeta.getId() + "/members")
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner cannot modify Team Beta details — returns 404")
    void alice_CannotModifyTeamBetaDetails() throws Exception {
        UpdateTeamRequest updateRequest = UpdateTeamRequest.builder()
                .name("Compromised Beta Squad")
                .build();

        mockMvc.perform(put("/api/v1/teams/" + teamBeta.getId())
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner cannot delete Team Beta workspace — returns 404")
    void alice_CannotDeleteTeamBetaWorkspace() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/" + teamBeta.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner cannot add member to Team Beta — returns 404")
    void alice_CannotAddMemberToTeamBeta() throws Exception {
        mockMvc.perform(post("/api/v1/teams/" + teamBeta.getId() + "/members/" + aliceAlphaOwner.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner cannot remove member from Team Beta — returns 404")
    void alice_CannotRemoveMemberFromTeamBeta() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/" + teamBeta.getId() + "/members/" + bobBetaOwner.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner cannot change member roles in Team Beta — returns 404")
    void alice_CannotChangeRolesInTeamBeta() throws Exception {
        UpdateTeamMemberRoleRequest request = new UpdateTeamMemberRoleRequest(TeamRole.MEMBER);

        mockMvc.perform(put("/api/v1/teams/" + teamBeta.getId() + "/members/" + bobBetaOwner.getId() + "/role")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner querying goals with teamId of Team Beta returns 404")
    void alice_QueryingGoalsWithForeignTeamId_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/goals?teamId=" + teamBeta.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner querying tasks with teamId of Team Beta returns 404")
    void alice_QueryingTasksWithForeignTeamId_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/tasks?teamId=" + teamBeta.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Team Alpha owner querying progress with teamId of Team Beta returns 404")
    void alice_QueryingProgressWithForeignTeamId_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/progress?teamId=" + teamBeta.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

