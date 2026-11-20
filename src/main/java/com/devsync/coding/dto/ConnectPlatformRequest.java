package com.devsync.coding.dto;

import com.devsync.coding.enums.CodingPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ConnectPlatformRequest {

    @NotNull(message = "Platform is required")
    private CodingPlatform platform;

    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
    private String username;

    public ConnectPlatformRequest() {
    }

    public ConnectPlatformRequest(CodingPlatform platform, String username) {
        this.platform = platform;
        this.username = username;
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
}
