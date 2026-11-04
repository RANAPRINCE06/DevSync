package com.devsync.progress.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.progress.dto.CreateProgressRequest;
import com.devsync.progress.dto.UpdateProgressRequest;
import com.devsync.progress.entity.DailyProgress;
import com.devsync.progress.entity.ProgressStatus;
import com.devsync.progress.repository.DailyProgressRepository;
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
@DisplayName("ProgressOwnershipSecurityTest — Object-level authorization & IDOR protection")
class ProgressOwnershipSecurityTest {

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
    private DailyProgressRepository dailyProgressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private Team team;
    private DailyProgress progressA;
    private DailyProgress progressB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        dailyProgressRepository.deleteAll();
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
                .name("Progress Squad " + UUID.randomUUID())
                .description("Progress security team")
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

        progressA = dailyProgressRepository.save(DailyProgress.builder()
                .user(userA)
                .team(team)
                .progressDate(LocalDate.now())
                .whatStudied("Alice studied Spring Security architecture")
                .completed("Completed IDOR protection layer")
                .studyMinutes(180)
                .challenges("Complex cross-user testing")
                .improvementAreas("Centralized security")
                .tomorrowPlan("Implement team security")
                .status(ProgressStatus.COMPLETED)
                .build());

        progressB = dailyProgressRepository.save(DailyProgress.builder()
                .user(userB)
                .team(team)
                .progressDate(LocalDate.now())
                .whatStudied("Bob studied frontend architecture")
                .completed("Finished UI components")
                .studyMinutes(120)
                .challenges("State synchronization")
                .improvementAreas("Caching")
                .tomorrowPlan("Next sprint items")
                .status(ProgressStatus.COMPLETED)
                .build());

        tokenA = jwtTokenProvider.generateToken(userA.getId(), userA.getEmail(), List.of("USER"));
        tokenB = jwtTokenProvider.generateToken(userB.getId(), userB.getEmail(), List.of("USER"));
    }

    @Test
    @DisplayName("User A can retrieve their own progress by ID")
    void userA_CanRetrieveOwnProgress() throws Exception {
        mockMvc.perform(get("/api/v1/progress/" + progressA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(progressA.getId().toString()))
                .andExpect(jsonPath("$.data.whatStudied").value("Alice studied Spring Security architecture"));
    }

    @Test
    @DisplayName("User B cannot access User A's progress by ID — returns 404 Not Found")
    void userB_CannotAccessUserAProgress() throws Exception {
        mockMvc.perform(get("/api/v1/progress/" + progressA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Daily progress entry not found with id: " + progressA.getId())));
    }

    @Test
    @DisplayName("User B cannot update User A's progress — returns 404 Not Found")
    void userB_CannotUpdateUserAProgress() throws Exception {
        UpdateProgressRequest updateRequest = UpdateProgressRequest.builder()
                .whatStudied("Hacked study content")
                .completed("Malicious edit")
                .studyMinutes(10)
                .status(ProgressStatus.PARTIAL)
                .build();

        mockMvc.perform(put("/api/v1/progress/" + progressA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User A cannot create progress under User B's identity — returns 403 Forbidden")
    void userA_CannotCreateProgressForUserB() throws Exception {
        CreateProgressRequest createRequest = CreateProgressRequest.builder()
                .userId(userB.getId()) // attempting spoofing
                .teamId(team.getId())
                .progressDate(LocalDate.now().minusDays(1))
                .whatStudied("Spoofed progress by Alice")
                .completed("Completed nothing")
                .studyMinutes(60)
                .status(ProgressStatus.COMPLETED)
                .build();

        mockMvc.perform(post("/api/v1/progress")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User A can list their own progress, excluding User B's progress")
    void userA_CanListOwnProgress() throws Exception {
        mockMvc.perform(get("/api/v1/progress")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(progressA.getId().toString()))
                .andExpect(jsonPath("$.data.content[0].whatStudied").value("Alice studied Spring Security architecture"));
    }

    @Test
    @DisplayName("User A cannot query User B's progress via userId param — returns 403 Forbidden")
    void userA_CannotQueryUserBProgress() throws Exception {
        mockMvc.perform(get("/api/v1/progress?userId=" + userB.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
