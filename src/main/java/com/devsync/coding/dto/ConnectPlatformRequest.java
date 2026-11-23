package com.devsync.coding.dto;

import com.devsync.coding.enums.CodingPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to connect a competitive programming platform account either by URL or (platform, username)")
public class ConnectPlatformRequest {

    @Schema(description = "Coding platform enum (optional if profileUrl provided)")
    private CodingPlatform platform;

    @Size(max = 100, message = "Username must be up to 100 characters")
    @Schema(description = "Platform username / handle (optional if profileUrl provided)")
    private String username;

    @Schema(description = "Public profile URL (optional if platform and username provided)")
    private String profileUrl;

    public ConnectPlatformRequest() {
    }

    public ConnectPlatformRequest(CodingPlatform platform, String username) {
        this.platform = platform;
        this.username = username;
    }

    public ConnectPlatformRequest(CodingPlatform platform, String username, String profileUrl) {
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
    }

    public ConnectPlatformRequest(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
