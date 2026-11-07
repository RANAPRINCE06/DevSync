package com.devsync.team.security;

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
@DisplayName("CollaborativeTeamGoalSecurityTest — Team Goals collaboration & authorization matrix")
class CollaborativeTeamGoalSecurityTest {

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

    private User aliceOwner;
    private User charlieAdmin;
    private User daveMember;
    private User eveOutsider;

    private Team teamAlpha;
    private Team teamBeta;

    private Goal teamGoalAlpha;
    private Goal alicePersonalGoal;
    private Goal davePersonalGoal;

    private String tokenOwner;
    private String tokenAdmin;
    private String tokenMember;
    private String tokenOutsider;

    @BeforeEach
    void setUp() {
        goalRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();

        String encodedPassword = passwordEncoder.encode("Password123!");

        aliceOwner = userRepository.save(User.builder()
                .name("Alice Owner")
                .email("alice-owner-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        charlieAdmin = userRepository.save(User.builder()
                .name("Charlie Admin")
                .email("charlie-admin-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        daveMember = userRepository.save(User.builder()
                .name("Dave Member")
                .email("dave-member-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        eveOutsider = userRepository.save(User.builder()
                .name("Eve Outsider")
                .email("eve-outsider-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        teamAlpha = teamRepository.save(Team.builder()
                .name("Alpha Squad " + UUID.randomUUID())
                .description("Primary squad")
                .active(true)
                .build());

        teamBeta = teamRepository.save(Team.builder()
                .name("Beta Squad " + UUID.randomUUID())
                .description("Foreign squad")
                .active(true)
                .build());

        // Team Alpha memberships
        teamMemberRepository.save(TeamMember.builder().user(aliceOwner).team(teamAlpha).role(TeamRole.OWNER).active(true).build());
        teamMemberRepository.save(TeamMember.builder().user(charlieAdmin).team(teamAlpha).role(TeamRole.ADMIN).active(true).build());
        teamMemberRepository.save(TeamMember.builder().user(daveMember).team(teamAlpha).role(TeamRole.MEMBER).active(true).build());

        // Team Beta membership for outsider
        teamMemberRepository.save(TeamMember.builder().user(eveOutsider).team(teamBeta).role(TeamRole.MEMBER).active(true).build());

        // Collaborative Team Goal in Alpha
        teamGoalAlpha = goalRepository.save(Goal.builder()
                .owner(aliceOwner)
                .team(teamAlpha)
                .title("Alpha Shared Architecture Goal")
                .description("Collaborative milestone for Alpha Squad")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.HIGH)
                .progressPercentage(30)
                .active(true)
                .teamGoal(true)
                .build());

        // Personal Goal of Alice in Alpha
        alicePersonalGoal = goalRepository.save(Goal.builder()
                .owner(aliceOwner)
                .team(teamAlpha)
                .title("Alice's Private Personal Goal")
                .description("Confidential to Alice")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(2))
                .status(GoalStatus.IN_PROGRESS)
                .priority(GoalPriority.MEDIUM)
                .progressPercentage(10)
                .active(true)
                .teamGoal(false)
                .build());

        // Personal Goal of Dave in Alpha
        davePersonalGoal = goalRepository.save(Goal.builder()
                .owner(daveMember)
                .team(teamAlpha)
                .title("Dave's Private Personal Goal")
                .description("Confidential to Dave")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(2))
                .status(GoalStatus.NOT_STARTED)
                .priority(GoalPriority.LOW)
                .progressPercentage(0)
                .active(true)
                .teamGoal(false)
                .build());

        tokenOwner = jwtTokenProvider.generateToken(aliceOwner.getId(), aliceOwner.getEmail(), List.of("USER"));
        tokenAdmin = jwtTokenProvider.generateToken(charlieAdmin.getId(), charlieAdmin.getEmail(), List.of("USER"));
        tokenMember = jwtTokenProvider.generateToken(daveMember.getId(), daveMember.getEmail(), List.of("USER"));
        tokenOutsider = jwtTokenProvider.generateToken(eveOutsider.getId(), eveOutsider.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("Active team member can view collaborative Team Goal")
    void activeMember_CanViewTeamGoal() throws Exception {
        mockMvc.perform(get("/api/v1/goals/" + teamGoalAlpha.getId())
                        .header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(teamGoalAlpha.getId().toString()))
                .andExpect(jsonPath("$.data.title").value("Alpha Shared Architecture Goal"))
                .andExpect(jsonPath("$.data.teamGoal").value(true));
    }

    @Test
    @DisplayName("Team Admin can view collaborative Team Goal")
    void activeAdmin_CanViewTeamGoal() throws Exception {
        mockMvc.perform(get("/api/v1/goals/" + teamGoalAlpha.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(teamGoalAlpha.getId().toString()));
    }

    @Test
    @DisplayName("Outsider cannot view collaborative Team Goal — returns 404 (Zero Leakage)")
    void outsider_CannotViewTeamGoal_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/goals/" + teamGoalAlpha.getId())
                        .header("Authorization", "Bearer " + tokenOutsider))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Team Owner can create a collaborative Team Goal")
    void owner_CanCreateTeamGoal() throws Exception {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(aliceOwner.getId())
                .teamId(teamAlpha.getId())
                .title("New Team Goal by Owner")
                .description("Team milestone")
                .priority(GoalPriority.HIGH)
                .teamGoal(true)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + tokenOwner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Team Goal by Owner"))
                .andExpect(jsonPath("$.data.teamGoal").value(true));
    }

    @Test
    @DisplayName("Team Admin can create a collaborative Team Goal")
    void admin_CanCreateTeamGoal() throws Exception {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(charlieAdmin.getId())
                .teamId(teamAlpha.getId())
                .title("New Team Goal by Admin")
                .description("Admin milestone")
                .priority(GoalPriority.MEDIUM)
                .teamGoal(true)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Team Goal by Admin"))
                .andExpect(jsonPath("$.data.teamGoal").value(true));
    }

    @Test
    @DisplayName("Regular Member CANNOT create a collaborative Team Goal — returns 403 Forbidden")
    void member_CannotCreateTeamGoal_Returns403() throws Exception {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(daveMember.getId())
                .teamId(teamAlpha.getId())
                .title("Unauthorized Team Goal by Member")
                .description("Member attempting to create team goal")
                .priority(GoalPriority.LOW)
                .teamGoal(true)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + tokenMember)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Outsider CANNOT create a Team Goal in foreign team — returns 404 Not Found")
    void outsider_CannotCreateTeamGoal_Returns404() throws Exception {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(eveOutsider.getId())
                .teamId(teamAlpha.getId())
                .title("Malicious Goal by Outsider")
                .priority(GoalPriority.CRITICAL)
                .teamGoal(true)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + tokenOutsider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Team Admin can update a collaborative Team Goal")
    void admin_CanUpdateTeamGoal() throws Exception {
        UpdateGoalRequest request = UpdateGoalRequest.builder()
                .title("Alpha Architecture Goal (Updated by Admin)")
                .description("Updated details")
                .priority(GoalPriority.CRITICAL)
                .status(GoalStatus.IN_PROGRESS)
                .progressPercentage(50)
                .build();

        mockMvc.perform(put("/api/v1/goals/" + teamGoalAlpha.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Alpha Architecture Goal (Updated by Admin)"))
                .andExpect(jsonPath("$.data.progressPercentage").value(50));
    }

    @Test
    @DisplayName("Regular Member CANNOT update a collaborative Team Goal — returns 403 Forbidden")
    void member_CannotUpdateTeamGoal_Returns403() throws Exception {
        UpdateGoalRequest request = UpdateGoalRequest.builder()
                .title("Tampered Goal Title")
                .priority(GoalPriority.LOW)
                .status(GoalStatus.IN_PROGRESS)
                .progressPercentage(99)
                .build();

        mockMvc.perform(put("/api/v1/goals/" + teamGoalAlpha.getId())
                        .header("Authorization", "Bearer " + tokenMember)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Regular Member CANNOT delete a collaborative Team Goal — returns 403 Forbidden")
    void member_CannotDeleteTeamGoal_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/goals/" + teamGoalAlpha.getId())
                        .header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Team Admin can delete (soft-delete) a collaborative Team Goal")
    void admin_CanDeleteTeamGoal() throws Exception {
        mockMvc.perform(delete("/api/v1/goals/" + teamGoalAlpha.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Team member CANNOT access another member's personal Goal in the same team — returns 404")
    void member_CannotAccessOtherMemberPersonalGoal_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/goals/" + alicePersonalGoal.getId())
                        .header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Team workspace listing returns collaborative Team Goals and own personal goals, never foreign personal goals")
    void teamWorkspaceListing_IsolatesPersonalGoals() throws Exception {
        mockMvc.perform(get("/api/v1/goals")
                        .param("teamId", teamAlpha.getId().toString())
                        .header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].title", hasItems(
                        "Alpha Shared Architecture Goal",
                        "Dave's Private Personal Goal"
                )))
                .andExpect(jsonPath("$.data.content[*].title", not(hasItem("Alice's Private Personal Goal"))));
    }
}

