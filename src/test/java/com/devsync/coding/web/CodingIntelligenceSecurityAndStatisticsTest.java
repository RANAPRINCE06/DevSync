package com.devsync.coding.web;

import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CodingIntelligenceSecurityAndStatisticsTest — IDOR Ownership Protection & Statistics Aggregation")
class CodingIntelligenceSecurityAndStatisticsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformAccountRepository platformAccountRepository;

    @Autowired
    private PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User alice;
    private User bob;
    private String aliceToken;
    private String bobToken;

    private PlatformAccount aliceAccount;
    private PlatformAccount bobAccount;

    @BeforeEach
    void setUp() {
        platformStatSnapshotRepository.deleteAll();
        platformAccountRepository.deleteAll();
        userRepository.deleteAll();

        alice = userRepository.save(new User(
                null,
                "Alice Engineer",
                "alice.security@devsync.com",
                passwordEncoder.encode("Password123!"),
                null,
                "UTC",
                true,
                Instant.now(),
                Instant.now()
        ));

        bob = userRepository.save(new User(
                null,
                "Bob Attacker",
                "bob.attacker@devsync.com",
                passwordEncoder.encode("Password123!"),
                null,
                "UTC",
                true,
                Instant.now(),
                Instant.now()
        ));

        aliceToken = jwtTokenProvider.generateToken(alice.getId(), alice.getEmail(), List.of("ROLE_USER"));
        bobToken = jwtTokenProvider.generateToken(bob.getId(), bob.getEmail(), List.of("ROLE_USER"));

        aliceAccount = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/u/alice_lc/")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        bobAccount = platformAccountRepository.save(PlatformAccount.builder()
                .user(bob)
                .platform(CodingPlatform.CODEFORCES)
                .username("bob_cf")
                .profileUrl("https://codeforces.com/profile/bob_cf")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());
    }

    // ==========================================
    // Ownership Security & IDOR Protection Tests
    // ==========================================

    @Test
    @DisplayName("Unauthenticated request — Rejects with 401 Unauthorized across all endpoints")
    void unauthenticatedAccess_ThrowsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/coding/accounts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/coding/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\": \"LEETCODE\", \"username\": \"anon\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/coding/accounts/" + aliceAccount.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/coding/accounts/" + aliceAccount.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/coding/statistics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/coding/accounts/" + aliceAccount.getId() + "/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cross-user account access attempt — Bob querying Alice's account returns 404 Not Found (Zero Leakage)")
    void crossUserGetAccount_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/coding/accounts/" + aliceAccount.getId())
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Platform account not found")));
    }

    @Test
    @DisplayName("Cross-user account deletion attempt — Bob attempting to delete Alice's account returns 404 Not Found")
    void crossUserDeleteAccount_Returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/coding/accounts/" + aliceAccount.getId())
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Platform account not found")));

        // Verify Alice's account is still active
        PlatformAccount persisted = platformAccountRepository.findById(aliceAccount.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(persisted.isActive());
    }

    @Test
    @DisplayName("Cross-user history access attempt — Bob querying Alice's account history returns 404 Not Found")
    void crossUserGetHistory_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/coding/accounts/" + aliceAccount.getId() + "/history")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Platform account not found")));
    }

    @Test
    @DisplayName("Query non-existent account ID — Returns 404 Not Found")
    void getNonExistentAccount_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/coding/accounts/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // Statistics Aggregation Tests
    // ==========================================

    @Test
    @DisplayName("Get statistics for user without connected accounts — Returns empty metrics with 0 count")
    void getStatistics_NoAccounts_ReturnsEmptyMetrics() throws Exception {
        User charlie = userRepository.save(new User(
                null,
                "Charlie New",
                "charlie@devsync.com",
                passwordEncoder.encode("Password123!"),
                null,
                "UTC",
                true,
                Instant.now(),
                Instant.now()
        ));
        String charlieToken = jwtTokenProvider.generateToken(charlie.getId(), charlie.getEmail(), List.of("ROLE_USER"));

        mockMvc.perform(get("/api/v1/coding/statistics")
                        .header("Authorization", "Bearer " + charlieToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.connectedPlatformsCount", is(0)))
                .andExpect(jsonPath("$.data.totalProblemsSolved", nullValue()))
                .andExpect(jsonPath("$.data.totalContestsParticipated", nullValue()))
                .andExpect(jsonPath("$.data.highestRating", nullValue()))
                .andExpect(jsonPath("$.data.highestRatingPlatform", nullValue()))
                .andExpect(jsonPath("$.data.platformSnapshots", empty()));
    }

    @Test
    @DisplayName("Get statistics with snapshots — Aggregates solved counts, contest count, and highest rating without double-counting")
    void getStatistics_WithSnapshots_AggregatesCorrectly() throws Exception {
        // Alice connects Codeforces as well
        PlatformAccount cfAccount = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .profileUrl("https://codeforces.com/profile/alice_cf")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        // Alice's LeetCode snapshot: 450 problems, 1850 rating, 25 contests
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(aliceAccount)
                .currentRating(1850.0)
                .maximumRating(1900.0)
                .problemsSolved(450)
                .easySolved(200)
                .mediumSolved(200)
                .hardSolved(50)
                .contestsParticipated(25)
                .additionalStats(Map.of("ranking", "Top 5%"))
                .snapshotAt(Instant.now())
                .build());

        // Alice's Codeforces snapshot: 300 problems, 1950 rating, 40 contests
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(cfAccount)
                .currentRating(1950.0)
                .maximumRating(2010.0)
                .problemsSolved(300)
                .contestsParticipated(40)
                .snapshotAt(Instant.now())
                .build());

        mockMvc.perform(get("/api/v1/coding/statistics")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.connectedPlatformsCount", is(2)))
                .andExpect(jsonPath("$.data.totalProblemsSolved", is(750))) // 450 + 300
                .andExpect(jsonPath("$.data.totalContestsParticipated", is(65))) // 25 + 40
                .andExpect(jsonPath("$.data.highestRating", is(1950.0))) // CF 1950 > LC 1850
                .andExpect(jsonPath("$.data.highestRatingPlatform", is("CODEFORCES")))
                .andExpect(jsonPath("$.data.platformSnapshots", hasSize(2)));
    }

    @Test
    @DisplayName("Get account history — Returns chronological descending snapshots with pagination")
    void getAccountHistory_ReturnsChronologicalDescendingSnapshots() throws Exception {
        Instant now = Instant.now();

        // Older snapshot (10 days ago)
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(aliceAccount)
                .currentRating(1700.0)
                .problemsSolved(400)
                .snapshotAt(now.minus(10, ChronoUnit.DAYS))
                .build());

        // Newer snapshot (1 day ago)
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(aliceAccount)
                .currentRating(1850.0)
                .problemsSolved(450)
                .snapshotAt(now.minus(1, ChronoUnit.DAYS))
                .build());

        mockMvc.perform(get("/api/v1/coding/accounts/" + aliceAccount.getId() + "/history?page=0&size=10")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                // Most recent first
                .andExpect(jsonPath("$.data.content[0].currentRating", is(1850.0)))
                .andExpect(jsonPath("$.data.content[0].problemsSolved", is(450)))
                // Older second
                .andExpect(jsonPath("$.data.content[1].currentRating", is(1700.0)))
                .andExpect(jsonPath("$.data.content[1].problemsSolved", is(400)));
    }
}
