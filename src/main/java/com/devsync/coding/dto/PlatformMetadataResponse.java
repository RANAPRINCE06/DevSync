package com.devsync.coding.dto;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.enums.PlatformCapability;

import java.util.Set;

/**
 * Metadata descriptor for external competitive programming and developer platforms.
 */
public class PlatformMetadataResponse {

    private CodingPlatform platform;
    private String displayName;
    private String profileUrlBase;
    private boolean available;
    private boolean syncSupported;
    private boolean communityApi;
    private Set<PlatformCapability> capabilities;
    private String syncNote;

    public PlatformMetadataResponse() {
    }

    public PlatformMetadataResponse(CodingPlatform platform, String displayName, String profileUrlBase,
                                    boolean available, boolean syncSupported, boolean communityApi,
                                    Set<PlatformCapability> capabilities, String syncNote) {
        this.platform = platform;
        this.displayName = displayName;
        this.profileUrlBase = profileUrlBase;
        this.available = available;
        this.syncSupported = syncSupported;
        this.communityApi = communityApi;
        this.capabilities = capabilities;
        this.syncNote = syncNote;
    }

    public static PlatformMetadataResponse from(CodingPlatform platform) {
        String note = switch (platform) {
            case LEETCODE, CODEFORCES, GITHUB -> "Official/public API synchronization fully supported.";
            case ATCODER -> "Synchronized via community AtCoder Problems API (kenkoooo.com). Solved counts and rankings supported.";
            case CODECHEF -> "Direct automated synchronization requires registered partner OAuth2 credentials. Profile linking supported.";
            case HACKERRANK -> "Official API is restricted to HackerRank for Work enterprise recruitment. Profile linking supported.";
        };

        return new PlatformMetadataResponse(
                platform,
                platform.getDisplayName(),
                platform.getProfileUrlBase(),
                platform.isAvailable(),
                platform.isAutomatedSyncSupported(),
                platform.isCommunityApiSupported(),
                platform.getCapabilities(),
                note
        );
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileUrlBase() {
        return profileUrlBase;
    }

    public void setProfileUrlBase(String profileUrlBase) {
        this.profileUrlBase = profileUrlBase;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isSyncSupported() {
        return syncSupported;
    }

    public void setSyncSupported(boolean syncSupported) {
        this.syncSupported = syncSupported;
    }

    public boolean isCommunityApi() {
        return communityApi;
    }

    public void setCommunityApi(boolean communityApi) {
        this.communityApi = communityApi;
    }

    public Set<PlatformCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<PlatformCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public String getSyncNote() {
        return syncNote;
    }

    public void setSyncNote(String syncNote) {
        this.syncNote = syncNote;
    }
}
