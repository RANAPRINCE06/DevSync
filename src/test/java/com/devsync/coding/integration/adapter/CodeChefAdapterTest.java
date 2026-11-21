package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CodeChefAdapterTest — CodeChef Legitimate Integration Boundaries")
class CodeChefAdapterTest {

    private CodeChefAdapter adapter;

    @BeforeEach
    void setUp() {
        CodingIntegrationProperties properties = new CodingIntegrationProperties();
        properties.getCodechef().setEnabled(false); // Default: unsupported
        adapter = new CodeChefAdapter(properties);
    }

    @Test
    @DisplayName("Platform metadata — Reports CODECHEF")
    void getPlatform_ReturnsCodeChef() {
        assertEquals(CodingPlatform.CODECHEF, adapter.getPlatform());
        assertTrue(adapter.supports(CodingPlatform.CODECHEF));
    }

    @Test
    @DisplayName("Validate username — Correctly checks handle characters")
    void validateUsername_ChecksCharacters() {
        assertTrue(adapter.validateUsername("chef_coder"));
        assertTrue(adapter.validateUsername("validUser123"));
        assertFalse(adapter.validateUsername("no spaces"));
        assertFalse(adapter.validateUsername("ab")); // too short (<3)
    }

    @Test
    @DisplayName("Fetch profile without OAuth partner credentials — Throws PlatformUnsupportedException with clear explanation")
    void fetchProfileData_ThrowsUnsupportedException() {
        PlatformUnsupportedException ex = assertThrows(PlatformUnsupportedException.class, () ->
                adapter.fetchProfileData("chef_coder")
        );

        assertTrue(ex.getMessage().contains("OAuth2 partner application credentials"),
                "Exception message must clarify that official partner credentials are required and brittle scraping is restricted");
    }

    @Test
    @DisplayName("Fetch profile with invalid handle syntax — Throws PlatformIntegrationException")
    void fetchProfileData_InvalidHandle_ThrowsException() {
        assertThrows(PlatformIntegrationException.class, () ->
                adapter.fetchProfileData("invalid handle with spaces!")
        );
    }
}
