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
 * In accordance with DevSync Step 5B specifications:
 * - CodeChef deprecated its public open API and requires registered partner OAuth2 credentials.
 * - In keeping with security best practices, fragile HTML scraping and Cloudflare bypasses are strictly avoided.
 * - This adapter documents and enforces an explicit unavailable/unsupported state until official OAuth2 credentials are configured.
 */
@Component
public class CodeChefAdapter implements CodingPlatformAdapter {

    private final boolean enabled;

    public CodeChefAdapter(CodingIntegrationProperties properties) {
        this.enabled = properties.getCodechef().isEnabled();
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

        // When official partner OAuth credentials become available in a future release,
        // authenticated API queries against https://api.codechef.com will be orchestrated here.
        throw new PlatformUnsupportedException("CodeChef official API client credentials not configured.");
    }
}
