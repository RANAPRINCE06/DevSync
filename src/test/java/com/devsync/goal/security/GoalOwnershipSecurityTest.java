package com.devsync.goal.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.goal.dto.CreateGoalRequest;
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
@DisplayName("GoalOwnershipSecurityTest — Object-level authorization & IDOR protection")
class GoalOwnershipSecurityTest {

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
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private Team team;
    private Goal goalA;
    private Goal goalB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
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
                .name("Alpha Squad " + UUID.randomUUID())
                .description("Security test team")
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
                .title("Alice's Secret Goal")
                .description("Private milestone")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.HIGH)
                .progressPercentage(25)
                .active(true)
                .build());

        goalB = goalRepository.save(Goal.builder()
                .owner(userB)
                .team(team)
                .title("Bob's Public Goal")
                .description("Another goal")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .status(GoalStatus.NOT_STARTED)
                .priority(GoalPriority.MEDIUM)
                .progressPercentage(0)
                .active(true)
                .build());

        tokenA = jwtTokenProvider.generateToken(userA.getId(), userA.getEmail(), List.of("USER"));
        tokenB = jwtTokenProvider.generateToken(userB.getId(), userB.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("User A can retrieve their own goal by ID")
    void userA_CanRetrieveOwnGoal() throws Exception {
        mockMvc.perform(get("/api/v1/goals/" + goalA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(goalA.getId().toString()))
                .andExpect(jsonPath("$.data.title").value("Alice's Secret Goal"));
    }

    @Test
    @DisplayName("User B cannot retrieve User A's goal by ID — returns 404 Not Found")
    void userB_CannotRetrieveUserAGoal() throws Exception {
        mockMvc.perform(get("/api/v1/goals/" + goalA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Goal not found with id: " + goalA.getId())));
    }

    @Test
    @DisplayName("User B cannot update User A's goal — returns 404 Not Found")
    void userB_CannotUpdateUserAGoal() throws Exception {
        UpdateGoalRequest updateRequest = UpdateGoalRequest.builder()
                .title("Bob Hacked This Goal")
                .description("Malicious modification")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .priority(GoalPriority.CRITICAL)
                .progressPercentage(50)
                .status(GoalStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(put("/api/v1/goals/" + goalA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User B cannot delete/deactivate User A's goal — returns 404 Not Found")
    void userB_CannotDeleteUserAGoal() throws Exception {
        mockMvc.perform(delete("/api/v1/goals/" + goalA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User B cannot modify User A's goal progress — returns 404 Not Found")
    void userB_CannotModifyUserAGoalProgress() throws Exception {
        UpdateGoalRequest updateRequest = UpdateGoalRequest.builder()
                .title("Alice's Secret Goal")
                .description("Private milestone")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .priority(GoalPriority.HIGH)
                .progressPercentage(100)
                .status(GoalStatus.COMPLETED)
                .build();

        mockMvc.perform(put("/api/v1/goals/" + goalA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Ownership cannot be overwritten through request data — User A cannot create goal for User B")
    void userA_CannotCreateGoalForUserB() throws Exception {
        CreateGoalRequest createRequest = CreateGoalRequest.builder()
                .ownerId(userB.getId()) // attempting spoofing
                .teamId(team.getId())
                .title("Spoofed Goal by Alice")
                .description("Should be rejected")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(2))
                .priority(GoalPriority.MEDIUM)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User A can list their own goals, excluding User B's goals")
    void userA_CanListOwnGoals() throws Exception {
        mockMvc.perform(get("/api/v1/goals")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(goalA.getId().toString()))
                .andExpect(jsonPath("$.data.content[0].title").value("Alice's Secret Goal"));
    }

    @Test
    @DisplayName("User A cannot query User B's goals via ownerId param — returns 403 Forbidden")
    void userA_CannotQueryUserBGoals() throws Exception {
        mockMvc.perform(get("/api/v1/goals?ownerId=" + userB.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
