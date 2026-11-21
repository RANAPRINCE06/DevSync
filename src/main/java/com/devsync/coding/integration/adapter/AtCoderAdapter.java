package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import org.springframework.stereotype.Component;

@Component
public class AtCoderAdapter implements CodingPlatformAdapter {

    private final boolean enabled;

    public AtCoderAdapter(CodingIntegrationProperties properties) {
        this.enabled = properties.getAtcoder().isEnabled();
    }

    @Override
    public CodingPlatform getPlatform() {
        return CodingPlatform.ATCODER;
    }

    @Override
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,16}$");
    }

    @Override
    public ExternalProfileData fetchProfileData(String username) {
        if (!validateUsername(username)) {
            throw new PlatformIntegrationException("Invalid AtCoder username format: " + username);
        }

        if (!enabled) {
            throw new PlatformUnsupportedException(
                    "AtCoder does not expose an official public REST API. Synchronization is disabled to prevent brittle scraping."
            );
        }

        throw new PlatformUnsupportedException("AtCoder integration not active.");
    }
}
