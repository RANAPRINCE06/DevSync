package com.devsync.coding.web;

import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.coding.dto.ConnectPlatformRequest;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.reliability.cache.PlatformProfileCache;
import com.devsync.coding.repository.PlatformAccountRepository;
import com.devsync.coding.repository.PlatformStatSnapshotRepository;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CodingIntelligenceComprehensiveSecurityAuditTest — Full Authorization, IDOR, & Edge Case Audit")
class CodingIntelligenceComprehensiveSecurityAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired(required = false)
    private PlatformProfileCache platformProfileCache;

    private User alice;
    private User bob;
    private String aliceToken;
    private String bobToken;

    private PlatformAccount aliceLcAccount;
    private PlatformAccount aliceCfAccount;

    @BeforeEach
    void setUp() {
        if (platformProfileCache != null) {
            platformProfileCache.clear();
        }
        platformStatSnapshotRepository.deleteAll();
        platformAccountRepository.deleteAll();
        userRepository.deleteAll();

        alice = userRepository.save(new User(
                null, "Alice Security", "alice.sec@devsync.com",
                passwordEncoder.encode("Password123!"), null, "UTC", true,
                Instant.now(), Instant.now()
        ));

        bob = userRepository.save(new User(
                null, "Bob Attacker", "bob.attacker@devsync.com",
                passwordEncoder.encode("Password123!"), null, "UTC", true,
                Instant.now(), Instant.now()
        ));

        aliceToken = jwtTokenProvider.generateToken(alice.getId(), alice.getEmail(), List.of("ROLE_USER"));
        bobToken = jwtTokenProvider.generateToken(bob.getId(), bob.getEmail(), List.of("ROLE_USER"));

        aliceLcAccount = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/u/alice_lc")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        aliceCfAccount = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .profileUrl("https://codeforces.com/profile/alice_cf")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(aliceLcAccount)
                .problemsSolved(450)
                .easySolved(150)
                .mediumSolved(200)
                .hardSolved(100)
                .currentRating(1820.0)
                .maximumRating(1850.0)
                .globalRank(15000L)
                .contestsParticipated(12)
                .snapshotAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .build());
    }

    @Test
    @DisplayName("IDOR Defense — Bob cannot view Alice's snapshot history (returns 404)")
    void getHistory_CrossUser_Returns404NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/coding/accounts/" + aliceLcAccount.getId() + "/history")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("IDOR Defense — Bob cannot trigger synchronization of Alice's account (returns 404)")
    void syncAccount_CrossUser_Returns404NotFound() throws Exception {
        mockMvc.perform(post("/api/v1/coding/accounts/" + aliceLcAccount.getId() + "/sync")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("IDOR Defense — Bob cannot disconnect Alice's account (returns 404)")
    void disconnectAccount_CrossUser_Returns404NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/coding/accounts/" + aliceLcAccount.getId())
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound());

        // Assert Alice's account remains active
        PlatformAccount persisted = platformAccountRepository.findById(aliceLcAccount.getId()).orElseThrow();
        assertTrue(persisted.isActive(), "Alice's account must remain active");
    }

    @Test
    @DisplayName("Identity derivation — User identity is taken strictly from JWT security context, not client input")
    void connectPlatform_IdentityDerivedFromPrincipal() throws Exception {
        ConnectPlatformRequest request = new ConnectPlatformRequest();
        request.setPlatform(CodingPlatform.GITHUB);
        request.setUsername("alice_gh");

        mockMvc.perform(post("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.platform", is("GITHUB")))
                .andExpect(jsonPath("$.data.username", is("alice_gh")));

        PlatformAccount created = platformAccountRepository
                .findByUserIdAndPlatformAndActiveTrue(alice.getId(), CodingPlatform.GITHUB)
                .orElseThrow();
        assertEquals(alice.getId(), created.getUser().getId(), "Account must belong to authenticated Alice");
    }

    @Test
    @DisplayName("Disconnected Account Isolation — Subsequent sync attempts on inactive accounts fail cleanly")
    void syncAccount_DisconnectedAccount_CannotBeSynced() throws Exception {
        // Alice disconnects her own account
        mockMvc.perform(delete("/api/v1/coding/accounts/" + aliceLcAccount.getId())
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk());

        // Subsequent sync attempt must return 404 because active=false
        mockMvc.perform(post("/api/v1/coding/accounts/" + aliceLcAccount.getId() + "/sync")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Analytics Tenant Isolation — Bob receives empty analytics data with 0 cross-user leak")
    void analyticsEndpoints_Bob_ReceivesEmptyData() throws Exception {
        // 1. Overview
        mockMvc.perform(get("/api/v1/coding/analytics/overview")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProblemsSolved", is(0)))
                .andExpect(jsonPath("$.data.connectedPlatformsCount", is(0)))
                .andExpect(jsonPath("$.data.highestRating", nullValue()))
                .andExpect(jsonPath("$.data.platformOverviews", hasSize(0)));

        // 2. Rating History
        mockMvc.perform(get("/api/v1/coding/analytics/rating-history?period=30d")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platforms", hasSize(0)));

        // 3. Problem Distribution
        mockMvc.perform(get("/api/v1/coding/analytics/problem-distribution?period=30d")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPlatformReportedSolved", is(0)))
                .andExpect(jsonPath("$.data.platformBreakdowns", hasSize(0)));

        // 4. Activity
        mockMvc.perform(get("/api/v1/coding/analytics/activity?period=30d")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSnapshotsInPeriod", is(0)))
                .andExpect(jsonPath("$.data.snapshotTimeline", hasSize(0)));

        // 5. Contest Performance
        mockMvc.perform(get("/api/v1/coding/analytics/contest-performance?period=30d")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalContestsParticipated", is(0)))
                .andExpect(jsonPath("$.data.platformContests", hasSize(0)));

        // 6. Insights
        mockMvc.perform(get("/api/v1/coding/analytics/insights?period=30d")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.insights", hasSize(0)));
    }

    @Test
    @DisplayName("Rating Isolation — Ratings are not combined or scaled across platforms")
    void ratingHistory_IndependentPlatforms_NeverCombined() throws Exception {
        // Add Codeforces snapshot
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(aliceCfAccount)
                .currentRating(1620.0)
                .maximumRating(1650.0)
                .snapshotAt(Instant.now())
                .build());

        mockMvc.perform(get("/api/v1/coding/analytics/rating-history?period=all")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platforms", hasSize(2)))
                .andExpect(jsonPath("$.data.platforms[?(@.platform == 'LEETCODE')].currentRating", contains(1820.0)))
                .andExpect(jsonPath("$.data.platforms[?(@.platform == 'CODEFORCES')].currentRating", contains(1620.0)));
    }

    @Test
    @DisplayName("Input Validation — Reversed date range or malformed period returns 400 Bad Request")
    void validation_ReversedDates_Returns400() throws Exception {
        Instant now = Instant.now();
        Instant past = now.minus(10, ChronoUnit.DAYS);

        // from is after to
        mockMvc.perform(get("/api/v1/coding/analytics/rating-history?from=" + now + "&to=" + past)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));

        // Invalid period string
        mockMvc.perform(get("/api/v1/coding/analytics/rating-history?period=invalid_period_string")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Authentication — Missing or tampered JWT returns 401 Unauthorized")
    void authentication_MissingOrInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/coding/accounts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer invalid.tampered.token"))
                .andExpect(status().isUnauthorized());
    }
}
