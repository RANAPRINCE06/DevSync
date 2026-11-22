package com.devsync.team.security;

import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TeamAccessSecurityTest — Workspace access control & team isolation")
class TeamAccessSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User ownerAlice;
    private User memberBob;
    private User nonMemberCharlie;
    private Team teamAlpha;
    private Team teamBeta;
    private String tokenAlice;
    private String tokenBob;
    private String tokenCharlie;

    @BeforeEach
    void setUp() {
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();

        String encodedPassword = passwordEncoder.encode("Password123!");

        ownerAlice = userRepository.save(User.builder()
                .name("Alice Owner")
                .email("alice-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        memberBob = userRepository.save(User.builder()
                .name("Bob Member")
                .email("bob-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        nonMemberCharlie = userRepository.save(User.builder()
                .name("Charlie Outsider")
                .email("charlie-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        teamAlpha = teamRepository.save(Team.builder()
                .name("Team Alpha " + UUID.randomUUID())
                .description("First squad")
                .active(true)
                .build());

        teamBeta = teamRepository.save(Team.builder()
                .name("Team Beta " + UUID.randomUUID())
                .description("Second squad")
                .active(true)
                .build());

        // Alice is OWNER of Alpha
        teamMemberRepository.save(TeamMember.builder()
                .user(ownerAlice)
                .team(teamAlpha)
                .role(TeamRole.OWNER)
                .active(true)
                .build());

        // Bob is MEMBER of Alpha
        teamMemberRepository.save(TeamMember.builder()
                .user(memberBob)
                .team(teamAlpha)
                .role(TeamRole.MEMBER)
                .active(true)
                .build());

        // Charlie is OWNER of Beta (foreign team)
        teamMemberRepository.save(TeamMember.builder()
                .user(nonMemberCharlie)
                .team(teamBeta)
                .role(TeamRole.OWNER)
                .active(true)
                .build());

        tokenAlice = jwtTokenProvider.generateToken(ownerAlice.getId(), ownerAlice.getEmail(), List.of("USER"));
        tokenBob = jwtTokenProvider.generateToken(memberBob.getId(), memberBob.getEmail(), List.of("USER"));
        tokenCharlie = jwtTokenProvider.generateToken(nonMemberCharlie.getId(), nonMemberCharlie.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("Owner can access their workspace by ID")
    void owner_CanAccessWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/teams/" + teamAlpha.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(teamAlpha.getId().toString()))
                .andExpect(jsonPath("$.data.name").value(teamAlpha.getName()));
    }

    @Test
    @DisplayName("Active member can access their authorized workspace by ID")
    void activeMember_CanAccessWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/teams/" + teamAlpha.getId())
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(teamAlpha.getId().toString()));
    }

    @Test
    @DisplayName("Non-member receives 404 Not Found when attempting to access private workspace")
    void nonMember_Receives404() throws Exception {
        mockMvc.perform(get("/api/v1/teams/" + teamAlpha.getId())
                        .header("Authorization", "Bearer " + tokenCharlie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User from another team receives 404 Not Found")
    void foreignTeamUser_Receives404() throws Exception {
        mockMvc.perform(get("/api/v1/teams/" + teamBeta.getId())
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Missing authentication receives 401 Unauthorized")
    void unauthenticated_Receives401() throws Exception {
        mockMvc.perform(get("/api/v1/teams/" + teamAlpha.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Listing teams scopes results strictly to teams the caller belongs to")
    void listTeams_ScopesToCallerTeams() throws Exception {
        // Alice only belongs to Alpha
        mockMvc.perform(get("/api/v1/teams")
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(teamAlpha.getId().toString()));

        // Charlie only belongs to Beta
        mockMvc.perform(get("/api/v1/teams")
                        .header("Authorization", "Bearer " + tokenCharlie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(teamBeta.getId().toString()));
    }

    @Test
    @DisplayName("Inactive/removed member receives 404 Not Found")
    void inactiveMember_Receives404() throws Exception {
        // Deactivate Bob in teamAlpha
        TeamMember bobsMembership = teamMemberRepository.findByUserIdAndTeamId(memberBob.getId(), teamAlpha.getId()).orElseThrow();
        bobsMembership.setActive(false);
        teamMemberRepository.save(bobsMembership);

        mockMvc.perform(get("/api/v1/teams/" + teamAlpha.getId())
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Inactive team blocks all member access — returns 404 Not Found")
    void inactiveTeam_BlocksMemberAccess_Returns404() throws Exception {
        // Deactivate teamAlpha completely
        teamAlpha.setActive(false);
        teamRepository.save(teamAlpha);

        // Even though Alice is OWNER and Bob is MEMBER, both receive 404
        mockMvc.perform(get("/api/v1/teams/" + teamAlpha.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/teams/" + teamAlpha.getId())
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

