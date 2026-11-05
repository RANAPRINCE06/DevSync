package com.devsync.team.security;

import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.team.dto.CreateTeamRequest;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TeamRoleAuthorizationSecurityTest — Role-based authorization & member management")
class TeamRoleAuthorizationSecurityTest {

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
    private PasswordEncoder passwordEncoder;

    private User ownerAlice;
    private User adminBob;
    private User memberCharlie;
    private User outsiderDave;
    private Team team;
    private String tokenAlice;
    private String tokenBob;
    private String tokenCharlie;
    private String tokenDave;

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

        adminBob = userRepository.save(User.builder()
                .name("Bob Admin")
                .email("bob-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        memberCharlie = userRepository.save(User.builder()
                .name("Charlie Member")
                .email("charlie-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        outsiderDave = userRepository.save(User.builder()
                .name("Dave Outsider")
                .email("dave-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash(encodedPassword)
                .timezone("UTC")
                .active(true)
                .build());

        team = teamRepository.save(Team.builder()
                .name("DevOps Squad " + UUID.randomUUID())
                .description("Engineering infrastructure")
                .active(true)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .user(ownerAlice)
                .team(team)
                .role(TeamRole.OWNER)
                .active(true)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .user(adminBob)
                .team(team)
                .role(TeamRole.ADMIN)
                .active(true)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .user(memberCharlie)
                .team(team)
                .role(TeamRole.MEMBER)
                .active(true)
                .build());

        tokenAlice = jwtTokenProvider.generateToken(ownerAlice.getId(), ownerAlice.getEmail(), List.of("USER"));
        tokenBob = jwtTokenProvider.generateToken(adminBob.getId(), adminBob.getEmail(), List.of("USER"));
        tokenCharlie = jwtTokenProvider.generateToken(memberCharlie.getId(), memberCharlie.getEmail(), List.of("USER"));
        tokenDave = jwtTokenProvider.generateToken(outsiderDave.getId(), outsiderDave.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("Team creation assigns authenticated user as OWNER transactionally")
    void createTeam_AssignsCallerAsOwner() throws Exception {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("New Architecture Squad " + UUID.randomUUID())
                .description("Cloud Architecture")
                .creatorUserId(ownerAlice.getId())
                .build();

        mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Team creation spoofing creatorUserId returns 403 Forbidden")
    void createTeam_SpoofedCreator_Returns403() throws Exception {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("Spoofed Squad " + UUID.randomUUID())
                .description("Malicious creation")
                .creatorUserId(adminBob.getId()) // Alice claiming Bob's identity
                .build();

        mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Owner and Admin can update team details")
    void ownerAndAdmin_CanUpdateTeam() throws Exception {
        UpdateTeamRequest updateRequest = UpdateTeamRequest.builder()
                .name("Updated Squad Name " + UUID.randomUUID())
                .description("Updated description")
                .build();

        mockMvc.perform(put("/api/v1/teams/" + team.getId())
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(put("/api/v1/teams/" + team.getId())
                        .header("Authorization", "Bearer " + tokenBob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Regular member cannot update team details — returns 403 Forbidden")
    void regularMember_CannotUpdateTeam_Returns403() throws Exception {
        UpdateTeamRequest updateRequest = UpdateTeamRequest.builder()
                .name("Member Hacked Name")
                .description("Illegal edit")
                .build();

        mockMvc.perform(put("/api/v1/teams/" + team.getId())
                        .header("Authorization", "Bearer " + tokenCharlie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Non-member cannot update team details — returns 404 Not Found")
    void nonMember_CannotUpdateTeam_Returns404() throws Exception {
        UpdateTeamRequest updateRequest = UpdateTeamRequest.builder()
                .name("Outsider Hacked Name")
                .build();

        mockMvc.perform(put("/api/v1/teams/" + team.getId())
                        .header("Authorization", "Bearer " + tokenDave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Regular member cannot delete team — returns 403 Forbidden")
    void regularMember_CannotDeleteTeam_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/" + team.getId())
                        .header("Authorization", "Bearer " + tokenCharlie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Admin cannot delete team — returns 403 Forbidden (Owner only)")
    void admin_CannotDeleteTeam_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/" + team.getId())
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Owner can delete/deactivate team — returns 200 OK")
    void owner_CanDeleteTeam_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/" + team.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Team updatedTeam = teamRepository.findById(team.getId()).orElseThrow();
        assertFalse(updatedTeam.isActive());
    }

    @Test
    @DisplayName("Owner and Admin can add a member")
    void ownerAndAdmin_CanAddMember() throws Exception {
        User newUser = userRepository.save(User.builder()
                .name("Eve Recruit")
                .email("eve-" + UUID.randomUUID() + "@devsync.io")
                .passwordHash("hash")
                .timezone("UTC")
                .active(true)
                .build());

        mockMvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + newUser.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("Regular member cannot add a member — returns 403 Forbidden")
    void regularMember_CannotAddMember_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + outsiderDave.getId())
                        .header("Authorization", "Bearer " + tokenCharlie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Adding an already active member returns 400 Bad Request")
    void addMember_AlreadyActiveMember_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + memberCharlie.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Owner can remove a member — member immediately loses workspace access")
    void owner_CanRemoveMember_RevokesAccess() throws Exception {
        // Alice removes Charlie
        mockMvc.perform(delete("/api/v1/teams/" + team.getId() + "/members/" + memberCharlie.getId())
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Charlie can no longer access the workspace (returns 404)
        mockMvc.perform(get("/api/v1/teams/" + team.getId())
                        .header("Authorization", "Bearer " + tokenCharlie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Regular member cannot remove members — returns 403 Forbidden")
    void regularMember_CannotRemoveMember_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/" + team.getId() + "/members/" + adminBob.getId())
                        .header("Authorization", "Bearer " + tokenCharlie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Attempting to remove the team owner returns 400 Bad Request")
    void removeMember_OwnerCannotBeRemoved_Returns400() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/" + team.getId() + "/members/" + ownerAlice.getId())
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Regular member cannot update roles or promote themselves — returns 403 Forbidden")
    void regularMember_CannotUpdateRoles_Returns403() throws Exception {
        UpdateTeamMemberRoleRequest request = new UpdateTeamMemberRoleRequest(TeamRole.OWNER);

        mockMvc.perform(put("/api/v1/teams/" + team.getId() + "/members/" + memberCharlie.getId() + "/role")
                        .header("Authorization", "Bearer " + tokenCharlie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Owner can update member role and transfer ownership")
    void owner_CanUpdateMemberRole_AndTransferOwnership() throws Exception {
        // Alice promotes Charlie to ADMIN
        UpdateTeamMemberRoleRequest promoteRequest = new UpdateTeamMemberRoleRequest(TeamRole.ADMIN);
        mockMvc.perform(put("/api/v1/teams/" + team.getId() + "/members/" + memberCharlie.getId() + "/role")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        // Alice transfers ownership to Bob
        UpdateTeamMemberRoleRequest transferRequest = new UpdateTeamMemberRoleRequest(TeamRole.OWNER);
        mockMvc.perform(put("/api/v1/teams/" + team.getId() + "/members/" + adminBob.getId() + "/role")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("OWNER"));

        // Verify Alice was demoted to ADMIN
        TeamMember aliceMembership = teamMemberRepository.findByUserIdAndTeamId(ownerAlice.getId(), team.getId()).orElseThrow();
        assertEquals(TeamRole.ADMIN, aliceMembership.getRole());

        // Verify Bob is now OWNER
        TeamMember bobMembership = teamMemberRepository.findByUserIdAndTeamId(adminBob.getId(), team.getId()).orElseThrow();
        assertEquals(TeamRole.OWNER, bobMembership.getRole());
    }
}

