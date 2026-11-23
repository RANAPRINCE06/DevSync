package com.devsync.coding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to detect platform and preview public profile from URL")
public class ProfilePreviewRequest {

    @NotBlank(message = "Profile URL is required")
    @Schema(description = "Public profile URL from supported platform", example = "https://leetcode.com/u/tourist/")
    private String profileUrl;

    public ProfilePreviewRequest() {
    }

    public ProfilePreviewRequest(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
