package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import org.springframework.stereotype.Component;

/**
 * HackerRank adapter enforcing legitimate integration boundaries.
 *
 * In accordance with DevSync Step 6A specifications:
 * - HackerRank official REST APIs are restricted exclusively to enterprise assessment workflows (HackerRank for Work).
 * - Individual developer challenge profiles and badges cannot be synchronized without fragile/prohibited HTML scraping.
 * - This adapter enforces profile linking while explicitly disabling individual statistics synchronization.
 */
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

        throw new PlatformUnsupportedException(
                "HackerRank official APIs are restricted exclusively to enterprise assessment workflows (HackerRank for Work). " +
                "Individual developer challenge profiles cannot be synchronized without prohibited HTML scraping."
        );
    }
}
