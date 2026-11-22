package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HackerRankAdapterTest — HackerRank Integration Safety Boundaries")
class HackerRankAdapterTest {

    private HackerRankAdapter adapter;
    private CodingIntegrationProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CodingIntegrationProperties();
        properties.getHackerrank().setEnabled(false); // Default: unsupported
        adapter = new HackerRankAdapter(properties);
    }

    @Test
    @DisplayName("Platform metadata — Reports HACKERRANK")
    void getPlatform_ReturnsHackerRank() {
        assertEquals(CodingPlatform.HACKERRANK, adapter.getPlatform());
        assertTrue(adapter.supports(CodingPlatform.HACKERRANK));
    }

    @Test
    @DisplayName("Validate username — Correctly checks handle characters")
    void validateUsername_ChecksCharacters() {
        assertTrue(adapter.validateUsername("hacker_123"));
        assertTrue(adapter.validateUsername("developer_alice"));
        assertFalse(adapter.validateUsername("no spaces"));
        assertFalse(adapter.validateUsername("ab")); // too short (<3)
        assertFalse(adapter.validateUsername(null));
    }

    @Test
    @DisplayName("Fetch profile without enterprise auth — Throws PlatformUnsupportedException with clear explanation")
    void fetchProfileData_ThrowsUnsupportedException() {
        PlatformUnsupportedException ex = assertThrows(PlatformUnsupportedException.class, () ->
                adapter.fetchProfileData("hacker_123")
        );

        assertTrue(ex.getMessage().contains("HackerRank"),
                "Exception message must clarify enterprise authorization / restricted scraping");
    }

    @Test
    @DisplayName("Fetch profile when enabled — Explains HackerRank for Work enterprise boundary")
    void fetchProfileData_WhenEnabled_ThrowsEnterpriseBoundaryException() {
        properties.getHackerrank().setEnabled(true);
        HackerRankAdapter enabledAdapter = new HackerRankAdapter(properties);

        PlatformUnsupportedException ex = assertThrows(PlatformUnsupportedException.class, () ->
                enabledAdapter.fetchProfileData("hacker_123")
        );

        assertTrue(ex.getMessage().contains("enterprise assessment workflows") || ex.getMessage().contains("HackerRank for Work"));
    }

    @Test
    @DisplayName("Fetch profile with invalid handle syntax — Throws PlatformIntegrationException")
    void fetchProfileData_InvalidHandle_ThrowsException() {
        assertThrows(PlatformIntegrationException.class, () ->
                adapter.fetchProfileData("invalid handle with spaces!")
        );
    }
}
