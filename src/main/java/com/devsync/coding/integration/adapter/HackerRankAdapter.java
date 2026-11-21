package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import org.springframework.stereotype.Component;

@Component
public class HackerRankAdapter implements CodingPlatformAdapter {

    private final boolean enabled;

    public HackerRankAdapter(CodingIntegrationProperties properties) {
        this.enabled = properties.getHackerrank().isEnabled();
    }

    @Override
    public CodingPlatform getPlatform() {
        return CodingPlatform.HACKERRANK;
    }

    @Override
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,30}$");
    }

    @Override
    public ExternalProfileData fetchProfileData(String username) {
        if (!validateUsername(username)) {
            throw new PlatformIntegrationException("Invalid HackerRank username format: " + username);
        }

        if (!enabled) {
            throw new PlatformUnsupportedException(
                    "HackerRank public statistics synchronization is currently disabled. Official developer API requires enterprise authorization."
            );
        }

        throw new PlatformUnsupportedException("HackerRank integration not yet active.");
    }
}
