package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import org.springframework.stereotype.Component;

/**
 * CodeChef adapter enforcing legitimate integration boundaries.
 *
 * In accordance with DevSync Step 6A specifications:
 * - CodeChef deprecated open developer self-registration and requires registered partner OAuth2 credentials.
 * - Fragile HTML scraping and Cloudflare bypasses are strictly prohibited to comply with platform terms of service.
 * - This adapter enforces profile linking and informs users of OAuth2 partner credential requirements.
 */
@Component
public class CodeChefAdapter implements CodingPlatformAdapter {

    private final boolean enabled;
    private final String clientId;
    private final String clientSecret;

    public CodeChefAdapter(CodingIntegrationProperties properties) {
        this.enabled = properties.getCodechef().isEnabled();
        this.clientId = properties.getCodechef().getClientId();
        this.clientSecret = properties.getCodechef().getClientSecret();
    }

    @Override
    public CodingPlatform getPlatform() {
        return CodingPlatform.CODECHEF;
    }

    @Override
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,30}$");
    }

    @Override
    public ExternalProfileData fetchProfileData(String username) {
        if (!validateUsername(username)) {
            throw new PlatformIntegrationException("Invalid CodeChef username format: " + username);
        }

        if (!enabled) {
            throw new PlatformUnsupportedException(
                    "CodeChef integration requires official OAuth2 partner application credentials. " +
                    "Public scraping is restricted to comply with platform terms of service. Direct synchronization is currently unavailable."
            );
        }

        if (clientId == null || clientId.trim().isEmpty() || clientSecret == null || clientSecret.trim().isEmpty()) {
            throw new PlatformUnsupportedException(
                    "CodeChef official API client credentials not configured. " +
                    "Registered partner OAuth2 credentials (clientId, clientSecret) are required for automated synchronization."
            );
        }

        throw new PlatformUnsupportedException("CodeChef official partner OAuth2 synchronization flow not active.");
    }
}
