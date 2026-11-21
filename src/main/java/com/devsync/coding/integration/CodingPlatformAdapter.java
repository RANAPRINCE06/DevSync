package com.devsync.coding.integration;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.dto.ExternalProfileData;

/**
 * Common platform adapter contract for external developer and competitive programming platforms.
 */
public interface CodingPlatformAdapter {

    /**
     * Returns the coding platform supported by this adapter.
     */
    CodingPlatform getPlatform();

    /**
     * Checks if this adapter supports the given platform.
     */
    default boolean supports(CodingPlatform platform) {
        return getPlatform() == platform;
    }

    /**
     * Validates that the external username/handle conforms to platform syntax.
     */
    boolean validateUsername(String username);

    /**
     * Fetches public profile statistics and returns normalized data.
     *
     * @param username the external user handle
     * @return normalized external profile data with strictly nullable fields
     */
    ExternalProfileData fetchProfileData(String username);
}
