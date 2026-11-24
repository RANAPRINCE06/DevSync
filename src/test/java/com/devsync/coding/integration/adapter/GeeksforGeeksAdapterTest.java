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
@DisplayName("GeeksforGeeksAdapterTest — Community API Integration and Fault Tolerance")
class GeeksforGeeksAdapterTest {

    @Mock
    private CodingPlatformClient client;

    private CodingIntegrationProperties properties;
    private GeeksforGeeksAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new CodingIntegrationProperties();
        properties.getGeeksforgeeks().setEnabled(true);
        properties.getGeeksforgeeks().setBaseUrl("https://geeks-for-geeks-api.vercel.app");
        adapter = new GeeksforGeeksAdapter(client, properties);
    }

    @Test
    @DisplayName("Platform metadata — Reports GEEKSFORGEEKS and supports enum")
    void getPlatform_ReturnsGeeksforGeeks() {
        assertEquals(CodingPlatform.GEEKSFORGEEKS, adapter.getPlatform());
        assertTrue(adapter.supports(CodingPlatform.GEEKSFORGEEKS));
    }

    @Test
    @DisplayName("Validate username — Enforces GeeksforGeeks handle constraints")
    void validateUsername_EnforcesConstraints() {
        assertTrue(adapter.validateUsername("tourist_dev"));
        assertTrue(adapter.validateUsername("gfg_coder99"));
        assertTrue(adapter.validateUsername("alex-smith"));

        assertFalse(adapter.validateUsername(null));
        assertFalse(adapter.validateUsername("ab")); // < 3 chars
        assertFalse(adapter.validateUsername("invalid user space"));
    }

    @Test
    @DisplayName("Fetch profile data — Successfully parses info and solvedStats difficulty tiers")
    void fetchProfileData_Success() {
        GeeksforGeeksAdapter.GfgApiResponse resp = new GeeksforGeeksAdapter.GfgApiResponse();

        GeeksforGeeksAdapter.GfgInfo info = new GeeksforGeeksAdapter.GfgInfo();
        info.setUserName("gfg_master");
        info.setProfilePicture("https://media.geeksforgeeks.org/img-practice/user.svg");
        info.setCodingScore("1450");
        info.setTotalProblemsSolved("385");
        info.setInstituteRank("42");
        info.setCurrentStreak("15");
        info.setMaxStreak("90");
        info.setInstitution("Tech Institute");
        resp.setInfo(info);

        GeeksforGeeksAdapter.GfgSolvedStats stats = new GeeksforGeeksAdapter.GfgSolvedStats();
        stats.setSchool(new GeeksforGeeksAdapter.GfgCountEntry(15));
        stats.setBasic(new GeeksforGeeksAdapter.GfgCountEntry(50));
        stats.setEasy(new GeeksforGeeksAdapter.GfgCountEntry(120));
        stats.setMedium(new GeeksforGeeksAdapter.GfgCountEntry(140));
        stats.setHard(new GeeksforGeeksAdapter.GfgCountEntry(60));
        resp.setSolvedStats(stats);

        when(client.get(contains("gfg_master"), eq(GeeksforGeeksAdapter.GfgApiResponse.class), any()))
                .thenReturn(resp);

        ExternalProfileData data = adapter.fetchProfileData("gfg_master");

        assertNotNull(data);
        assertEquals(CodingPlatform.GEEKSFORGEEKS, data.getPlatform());
        assertEquals("gfg_master", data.getUsername());
        assertEquals("https://www.geeksforgeeks.org/user/gfg_master/", data.getProfileUrl());
        assertEquals(385, data.getProblemsSolved());

        // Difficulty mapping: school (15) + basic (50) + easy (120) = 185
        assertEquals(185, data.getEasySolved());
        assertEquals(140, data.getMediumSolved());
        assertEquals(60, data.getHardSolved());

        // Invariants: contest rating is null
        assertNull(data.getCurrentRating());
        assertNull(data.getMaximumRating());
        assertNull(data.getGlobalRank());
        assertNull(data.getContestsParticipated());

        // Additional stats
        assertNotNull(data.getAdditionalStats());
        assertEquals(1450, data.getAdditionalStats().get("codingScore"));
        assertEquals("42", data.getAdditionalStats().get("instituteRank"));
        assertEquals(15, data.getAdditionalStats().get("currentStreak"));
        assertEquals(90, data.getAdditionalStats().get("maxStreak"));
        assertEquals("Tech Institute", data.getAdditionalStats().get("institution"));
        assertEquals("gfg_master", data.getAdditionalStats().get("displayName"));
        assertEquals("https://media.geeksforgeeks.org/img-practice/user.svg", data.getAdditionalStats().get("avatarUrl"));
    }

    @Test
    @DisplayName("Fetch profile data — User not found raises PlatformUserNotFoundException")
    void fetchProfileData_UserNotFound() {
        when(client.get(contains("nonexistent"), eq(GeeksforGeeksAdapter.GfgApiResponse.class), any()))
                .thenThrow(new PlatformUserNotFoundException("Profile not found"));

        assertThrows(PlatformUserNotFoundException.class, () ->
                adapter.fetchProfileData("nonexistent")
        );
    }

    @Test
    @DisplayName("Fetch profile data — Disabled integration throws PlatformUnsupportedException")
    void fetchProfileData_Disabled_ThrowsException() {
        properties.getGeeksforgeeks().setEnabled(false);
        GeeksforGeeksAdapter disabledAdapter = new GeeksforGeeksAdapter(client, properties);

        assertThrows(PlatformUnsupportedException.class, () ->
                disabledAdapter.fetchProfileData("gfg_user")
        );
    }
}
