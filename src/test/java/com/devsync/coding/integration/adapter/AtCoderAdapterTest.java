package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtCoderAdapterTest — Community API Integration and Fault Tolerance")
class AtCoderAdapterTest {

    @Mock
    private CodingPlatformClient client;

    private CodingIntegrationProperties properties;
    private AtCoderAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new CodingIntegrationProperties();
        properties.getAtcoder().setEnabled(true);
        properties.getAtcoder().setBaseUrl("https://kenkoooo.com/atcoder/atcoder-api/v3");
        properties.getAtcoder().setRequestIntervalMs(0);
        adapter = new AtCoderAdapter(client, properties);
        adapter.setMinRequestIntervalMs(0);
    }

    @Test
    @DisplayName("Platform metadata — Reports ATCODER and supports enum")
    void getPlatform_ReturnsAtCoder() {
        assertEquals(CodingPlatform.ATCODER, adapter.getPlatform());
        assertTrue(adapter.supports(CodingPlatform.ATCODER));
    }

    @Test
    @DisplayName("Validate username — Enforces AtCoder username constraints")
    void validateUsername_EnforcesConstraints() {
        assertTrue(adapter.validateUsername("chokudai"));
        assertTrue(adapter.validateUsername("tourist_99"));
        assertTrue(adapter.validateUsername("abc"));

        assertFalse(adapter.validateUsername(null));
        assertFalse(adapter.validateUsername("ab")); // too short (<3)
        assertFalse(adapter.validateUsername("toolongusernameforatcoder123456789")); // > 16
        assertFalse(adapter.validateUsername("invalid-dash")); // only a-z, A-Z, 0-9, _
        assertFalse(adapter.validateUsername("invalid space"));
    }

    @Test
    @DisplayName("Fetch profile data — Successfully retrieves AC count, rankings and streaks")
    void fetchProfileData_Success() {
        AtCoderAdapter.AtCoderRankResponse acResp = new AtCoderAdapter.AtCoderRankResponse(320, 1540);
        AtCoderAdapter.AtCoderRankResponse pointResp = new AtCoderAdapter.AtCoderRankResponse(125000, 980);
        AtCoderAdapter.AtCoderRankResponse streakResp = new AtCoderAdapter.AtCoderRankResponse(42, 310);

        when(client.get(contains("/user/ac_rank"), eq(AtCoderAdapter.AtCoderRankResponse.class), any()))
                .thenReturn(acResp);
        when(client.get(contains("/user/rated_point_sum_rank"), eq(AtCoderAdapter.AtCoderRankResponse.class), any()))
                .thenReturn(pointResp);
        when(client.get(contains("/user/streak_rank"), eq(AtCoderAdapter.AtCoderRankResponse.class), any()))
                .thenReturn(streakResp);

        ExternalProfileData data = adapter.fetchProfileData("chokudai");

        assertNotNull(data);
        assertEquals(CodingPlatform.ATCODER, data.getPlatform());
        assertEquals("chokudai", data.getUsername());
        assertEquals("https://atcoder.jp/users/chokudai", data.getProfileUrl());
        assertEquals(320, data.getProblemsSolved());
        assertEquals(1540L, data.getGlobalRank());

        // Invariants: contest rating and difficulty breakdown are not provided by community AC API
        assertNull(data.getCurrentRating());
        assertNull(data.getMaximumRating());
        assertNull(data.getEasySolved());
        assertNull(data.getMediumSolved());
        assertNull(data.getHardSolved());
        assertNull(data.getContestsParticipated());

        // Community metrics verified in additionalStats
        assertNotNull(data.getAdditionalStats());
        assertTrue(data.getAdditionalStats().get("dataSource").toString().contains("kenkoooo.com"));
        assertEquals(1540L, data.getAdditionalStats().get("communityAcRank"));
        assertEquals(125000, data.getAdditionalStats().get("ratedPointSum"));
        assertEquals(980, data.getAdditionalStats().get("ratedPointSumRank"));
        assertEquals(42, data.getAdditionalStats().get("longestStreakDays"));
        assertEquals(310, data.getAdditionalStats().get("streakRank"));
    }

    @Test
    @DisplayName("Fetch profile data — User not found raises PlatformUserNotFoundException")
    void fetchProfileData_UserNotFound() {
        when(client.get(contains("/user/ac_rank"), eq(AtCoderAdapter.AtCoderRankResponse.class), any()))
                .thenThrow(new PlatformUserNotFoundException("Profile not found at external URL"));

        assertThrows(PlatformUserNotFoundException.class, () ->
                adapter.fetchProfileData("nonexistent_user")
        );
    }

    @Test
    @DisplayName("Fetch profile data — Degradation when secondary ranking endpoints fail")
    void fetchProfileData_SecondaryEndpointsFail_GracefulDegradation() {
        AtCoderAdapter.AtCoderRankResponse acResp = new AtCoderAdapter.AtCoderRankResponse(75, 4500);

        when(client.get(contains("/user/ac_rank"), eq(AtCoderAdapter.AtCoderRankResponse.class), any()))
                .thenReturn(acResp);
        when(client.get(contains("/user/rated_point_sum_rank"), eq(AtCoderAdapter.AtCoderRankResponse.class), any()))
                .thenThrow(new PlatformIntegrationException("Point sum service timed out"));
        when(client.get(contains("/user/streak_rank"), eq(AtCoderAdapter.AtCoderRankResponse.class), any()))
                .thenThrow(new PlatformIntegrationException("Streak service 500"));

        ExternalProfileData data = adapter.fetchProfileData("chokudai");

        assertNotNull(data);
        assertEquals(75, data.getProblemsSolved());
        assertEquals(4500L, data.getGlobalRank());
        assertNull(data.getAdditionalStats().get("ratedPointSum"));
        assertNull(data.getAdditionalStats().get("longestStreakDays"));
        assertEquals(true, data.getAdditionalStats().get("partialData"));
        assertEquals("PARTIAL", data.getAdditionalStats().get("secondaryDataStatus"));
    }

    @Test
    @DisplayName("Fetch profile data — Invalid username format throws PlatformIntegrationException")
    void fetchProfileData_InvalidUsername_ThrowsException() {
        assertThrows(PlatformIntegrationException.class, () ->
                adapter.fetchProfileData("invalid user space")
        );
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("Fetch profile data — Disabled integration throws PlatformUnsupportedException")
    void fetchProfileData_Disabled_ThrowsException() {
        properties.getAtcoder().setEnabled(false);
        AtCoderAdapter disabledAdapter = new AtCoderAdapter(client, properties);

        PlatformUnsupportedException ex = assertThrows(PlatformUnsupportedException.class, () ->
                disabledAdapter.fetchProfileData("chokudai")
        );
        assertTrue(ex.getMessage().contains("disabled"));
        verifyNoInteractions(client);
    }
}
