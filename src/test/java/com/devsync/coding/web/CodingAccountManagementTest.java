package com.devsync.coding.web;

import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.coding.dto.ConnectPlatformRequest;
import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.SyncStatus;
import com.devsync.coding.repository.PlatformAccountRepository;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CodingAccountManagementTest — Account Connection and Management APIs")
class CodingAccountManagementTest {

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
    private PasswordEncoder passwordEncoder;

    private User alice;
    private User bob;
    private String aliceToken;
    private String bobToken;

    @BeforeEach
    void setUp() {
        platformAccountRepository.deleteAll();
        userRepository.deleteAll();

        alice = userRepository.save(new User(
                null,
                "Alice Engineer",
                "alice.coding@devsync.com",
                passwordEncoder.encode("Password123!"),
                null,
                "UTC",
                true,
                Instant.now(),
                Instant.now()
        ));

        bob = userRepository.save(new User(
                null,
                "Bob Coder",
                "bob.coding@devsync.com",
                passwordEncoder.encode("Password123!"),
                null,
                "UTC",
                true,
                Instant.now(),
                Instant.now()
        ));

        aliceToken = jwtTokenProvider.generateToken(alice.getId(), alice.getEmail(), List.of("ROLE_USER"));
        bobToken = jwtTokenProvider.generateToken(bob.getId(), bob.getEmail(), List.of("ROLE_USER"));
    }

    @Test
    @DisplayName("Connect valid platform account — Returns 201 with generated profile URL and NEVER_SYNCED status")
    void connectPlatform_ValidRequest_Success() throws Exception {
        ConnectPlatformRequest request = new ConnectPlatformRequest(CodingPlatform.LEETCODE, "alice_coder");

        mockMvc.perform(post("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("connected successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.platform", is("LEETCODE")))
                .andExpect(jsonPath("$.data.platformDisplayName", is("LeetCode")))
                .andExpect(jsonPath("$.data.username", is("alice_coder")))
                .andExpect(jsonPath("$.data.profileUrl", is("https://leetcode.com/u/alice_coder/")))
                .andExpect(jsonPath("$.data.active", is(true)))
                .andExpect(jsonPath("$.data.syncStatus", is("NEVER_SYNCED")))
                .andExpect(jsonPath("$.data.lastSyncedAt", nullValue()));
    }

    @Test
    @DisplayName("Connect duplicate platform account — Rejects with 400 Bad Request")
    void connectPlatform_DuplicatePlatform_ThrowsBadRequest() throws Exception {
        ConnectPlatformRequest request = new ConnectPlatformRequest(CodingPlatform.CODEFORCES, "alice_cf");

        mockMvc.perform(post("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempting to connect Codeforces again for Alice
        ConnectPlatformRequest duplicate = new ConnectPlatformRequest(CodingPlatform.CODEFORCES, "alice_cf_alt");

        mockMvc.perform(post("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Connect platform with invalid username — Rejects with 400 Bad Request")
    void connectPlatform_InvalidUsername_ThrowsBadRequest() throws Exception {
        ConnectPlatformRequest requestWithSpaces = new ConnectPlatformRequest(CodingPlatform.GITHUB, "invalid user name!");

        mockMvc.perform(post("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithSpaces)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Connect platform with missing platform — Rejects with 400 Bad Request")
    void connectPlatform_MissingPlatform_ThrowsBadRequest() throws Exception {
        String invalidJson = "{\"username\": \"valid_user\"}";

        mockMvc.perform(post("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("List connected accounts — Returns only authenticated user's active accounts")
    void listConnectedAccounts_ReturnsOnlyOwnAccounts() throws Exception {
        // Alice connects LeetCode and GitHub
        platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/u/alice_lc/")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.GITHUB)
                .username("alice_gh")
                .profileUrl("https://github.com/alice_gh")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        // Bob connects Codeforces
        platformAccountRepository.save(PlatformAccount.builder()
                .user(bob)
                .platform(CodingPlatform.CODEFORCES)
                .username("bob_cf")
                .profileUrl("https://codeforces.com/profile/bob_cf")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        mockMvc.perform(get("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].username", containsInAnyOrder("alice_lc", "alice_gh")))
                .andExpect(jsonPath("$.data[*].username", not(hasItem("bob_cf"))));
    }

    @Test
    @DisplayName("Get account details by ID — Returns owned account details")
    void getAccountById_OwnedAccount_Success() throws Exception {
        PlatformAccount account = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.ATCODER)
                .username("alice_at")
                .profileUrl("https://atcoder.jp/users/alice_at")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        mockMvc.perform(get("/api/v1/coding/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(account.getId().toString())))
                .andExpect(jsonPath("$.data.platform", is("ATCODER")))
                .andExpect(jsonPath("$.data.username", is("alice_at")));
    }

    @Test
    @DisplayName("Disconnect owned account — Soft-deletes account (active = false) preserving record")
    void disconnectAccount_OwnedAccount_Success() throws Exception {
        PlatformAccount account = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.HACKERRANK)
                .username("alice_hr")
                .profileUrl("https://www.hackerrank.com/profile/alice_hr")
                .active(true)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        mockMvc.perform(delete("/api/v1/coding/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("disconnected successfully")));

        PlatformAccount updated = platformAccountRepository.findById(account.getId()).orElseThrow();
        assertFalse(updated.isActive());

        // Account is no longer listed in active accounts
        mockMvc.perform(get("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Re-connecting platform after disconnecting — Succeeds because previous account is inactive")
    void connectPlatform_AfterDisconnect_AllowsNewActiveAccount() throws Exception {
        // Alice connects and disconnects HackerRank
        PlatformAccount account = platformAccountRepository.save(PlatformAccount.builder()
                .user(alice)
                .platform(CodingPlatform.HACKERRANK)
                .username("alice_old_hr")
                .profileUrl("https://www.hackerrank.com/profile/alice_old_hr")
                .active(false)
                .syncStatus(SyncStatus.NEVER_SYNCED)
                .build());

        ConnectPlatformRequest newRequest = new ConnectPlatformRequest(CodingPlatform.HACKERRANK, "alice_new_hr");

        mockMvc.perform(post("/api/v1/coding/accounts")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username", is("alice_new_hr")))
                .andExpect(jsonPath("$.data.active", is(true)));
    }
}
