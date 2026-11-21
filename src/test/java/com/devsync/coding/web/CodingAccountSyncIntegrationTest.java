package com.devsync.coding.web;

import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.entity.PlatformStatSnapshot;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.registry.PlatformIntegrationRegistry;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CodingAccountSyncIntegrationTest — Synchronization Endpoint & Snapshot Persistence")
class CodingAccountSyncIntegrationTest {

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

    @MockBean
    private PlatformIntegrationRegistry platformIntegrationRegistry;

    private User alice;
    private User bob;
    private String aliceToken;
    private String bobToken;

    @BeforeEach
    void setUp() {
        platformStatSnapshotRepository.deleteAll();
        platformAccountRepository.deleteAll();
        userRepository.deleteAll();

        alice = userRepository.save(new User(
                null, "Alice Developer", "alice.sync@devsync.com",
                passwordEncoder.encode("Password123!"), null, "UTC", true,
                Instant.now(), Instant.now()
        ));

        bob = userRepository.save(new User(
                null, "Bob Developer", "bob.sync@devsync.com",
                passwordEncoder.encode("Password123!"), null, "UTC", true,
                Instant.now(), Instant.now()
        ));

        aliceToken = jwtTokenProvider.generateToken(alice.getId(), alice.getEmail(), List.of("ROLE_USER"));
        bobToken = jwtTokenProvider.generateToken(bob.getId(), bob.getEmail(), List.of("ROLE_USER"));
    }

    @Test
    @DisplayName("Sync account — Success persists snapshot and updates sync status to SUCCESS")
    void syncAccount_Success_PersistsSnapshotAndUpdatesStatus() throws Exception {
        PlatformAccount account = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .profileUrl("https://codeforces.com/profile/alice_cf")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        CodingPlatformAdapter adapter = org.mockito.Mockito.mock(CodingPlatformAdapter.class);
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.CODEFORCES)).thenReturn(adapter);

        ExternalProfileData mockData = ExternalProfileData.builder()
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .profileUrl("https://codeforces.com/profile/alice_cf")
                .currentRating(1650.0)
                .maximumRating(1720.0)
                .platformRank("expert")
                .problemsSolved(210)
                .contestsParticipated(15)
                .additionalStats(Map.of("contribution", 45))
                .fetchedAt(Instant.now())
                .build();

        when(adapter.fetchProfileData("alice_cf")).thenReturn(mockData);

        mockMvc.perform(post("/api/v1/coding/accounts/" + account.getId() + "/sync")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.syncStatus", is("SUCCESS")))
                .andExpect(jsonPath("$.data.lastSyncedAt", notNullValue()))
                .andExpect(jsonPath("$.data.latestSnapshot.currentRating", is(1650.0)))
                .andExpect(jsonPath("$.data.latestSnapshot.problemsSolved", is(210)));

        // Verify persistence
        PlatformAccount updated = platformAccountRepository.findById(account.getId()).orElseThrow();
        assertEquals(SyncStatus.SUCCESS, updated.getSyncStatus());
        assertNotNull(updated.getLastSyncedAt());
        assertNull(updated.getLastSyncError());

        List<PlatformStatSnapshot> snapshots = platformStatSnapshotRepository.findAll();
        assertEquals(1, snapshots.size());
        assertEquals(1650.0, snapshots.getFirst().getCurrentRating());
        assertEquals(210, snapshots.getFirst().getProblemsSolved());
    }

    @Test
    @DisplayName("Sync account with unchanged stats — Does not insert duplicate snapshot")
    void syncAccount_UnchangedStats_AvoidsDuplicateSnapshot() throws Exception {
        PlatformAccount account = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/u/alice_lc/")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        // Pre-existing snapshot with same stats
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(account)
                .problemsSolved(300)
                .easySolved(150)
                .mediumSolved(120)
                .hardSolved(30)
                .currentRating(1800.0)
                .maximumRating(1800.0)
                .globalRank(25000L)
                .contestsParticipated(10)
                .snapshotAt(Instant.now().minusSeconds(3600))
                .build());

        CodingPlatformAdapter adapter = org.mockito.Mockito.mock(CodingPlatformAdapter.class);
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.LEETCODE)).thenReturn(adapter);

        ExternalProfileData mockData = ExternalProfileData.builder()
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/u/alice_lc/")
                .problemsSolved(300)
                .easySolved(150)
                .mediumSolved(120)
                .hardSolved(30)
                .currentRating(1800.0)
                .maximumRating(1800.0)
                .globalRank(25000L)
                .contestsParticipated(10)
                .fetchedAt(Instant.now())
                .build();

        when(adapter.fetchProfileData("alice_lc")).thenReturn(mockData);

        // Sync again
        mockMvc.perform(post("/api/v1/coding/accounts/" + account.getId() + "/sync")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.syncStatus", is("SUCCESS")));

        // Invariant: duplicate snapshot is avoided, count remains 1
        List<PlatformStatSnapshot> snapshots = platformStatSnapshotRepository.findAll();
        assertEquals(1, snapshots.size(), "Unchanged statistics must not produce duplicate snapshot rows");
    }

    @Test
    @DisplayName("Failed sync — Sets FAILED status, sanitized error, and preserves previous snapshot")
    void syncAccount_Failure_PreservesPreviousSnapshot() throws Exception {
        PlatformAccount account = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .profileUrl("https://codeforces.com/profile/alice_cf")
                .active(true)
                .syncStatus(SyncStatus.SUCCESS)
                .build());

        // Pre-existing historical snapshot
        platformStatSnapshotRepository.save(PlatformStatSnapshot.builder()
                .platformAccount(account)
                .currentRating(1600.0)
                .problemsSolved(180)
                .snapshotAt(Instant.now().minusSeconds(86400))
                .build());

        CodingPlatformAdapter adapter = org.mockito.Mockito.mock(CodingPlatformAdapter.class);
        when(platformIntegrationRegistry.getRequiredAdapter(CodingPlatform.CODEFORCES)).thenReturn(adapter);
        when(adapter.fetchProfileData("alice_cf"))
                .thenThrow(new PlatformIntegrationException("External platform connection timed out"));

        mockMvc.perform(post("/api/v1/coding/accounts/" + account.getId() + "/sync")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.syncStatus", is("FAILED")))
                .andExpect(jsonPath("$.data.lastSyncError", containsString("External platform connection timed out")))
                // Invariant: previous snapshot is preserved and returned
                .andExpect(jsonPath("$.data.latestSnapshot.currentRating", is(1600.0)))
                .andExpect(jsonPath("$.data.latestSnapshot.problemsSolved", is(180)));

        PlatformAccount updated = platformAccountRepository.findById(account.getId()).orElseThrow();
        assertEquals(SyncStatus.FAILED, updated.getSyncStatus());
        assertNotNull(updated.getLastSyncError());

        // Snapshot table is not wiped or overwritten with zeroes
        List<PlatformStatSnapshot> snapshots = platformStatSnapshotRepository.findAll();
        assertEquals(1, snapshots.size());
        assertEquals(1600.0, snapshots.getFirst().getCurrentRating());
    }

    // =========================================================================
    // Security & IDOR Authorization Tests
    // =========================================================================

    @Test
    @DisplayName("Unauthenticated sync request — Rejects with 401 Unauthorized")
    void syncAccount_Unauthenticated_ReturnsUnauthorized() throws Exception {
        PlatformAccount account = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.GITHUB)
                .username("alice_gh")
                .profileUrl("https://github.com/alice_gh")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        mockMvc.perform(post("/api/v1/coding/accounts/" + account.getId() + "/sync"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cross-user sync attempt — Rejects with 404 Not Found (Zero Information Leakage)")
    void syncAccount_CrossUser_ReturnsNotFound() throws Exception {
        // Alice owns this account
        PlatformAccount aliceAccount = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.CODEFORCES)
                .username("alice_cf")
                .profileUrl("https://codeforces.com/profile/alice_cf")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        // Bob attempts to synchronize Alice's account
        mockMvc.perform(post("/api/v1/coding/accounts/" + aliceAccount.getId() + "/sync")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Cross-user history access attempt — Rejects with 404 Not Found")
    void getHistory_CrossUser_ReturnsNotFound() throws Exception {
        PlatformAccount aliceAccount = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/u/alice_lc/")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        mockMvc.perform(get("/api/v1/coding/accounts/" + aliceAccount.getId() + "/history")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound());
    }
}
