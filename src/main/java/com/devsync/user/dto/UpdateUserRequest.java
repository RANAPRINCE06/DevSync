package com.devsync.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(example = "Prince Rana")
    private String name;

    @Schema(example = "https://example.com/avatar_new.jpg")
    private String avatarUrl;

    @NotBlank(message = "Timezone is required")
    @Schema(example = "Asia/Kolkata")
    private String timezone;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String name, String avatarUrl, String timezone) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.timezone = timezone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public static UpdateUserRequestBuilder builder() {
        return new UpdateUserRequestBuilder();
    }

    public static class UpdateUserRequestBuilder {
        private String name;
        private String avatarUrl;
        private String timezone;

        public UpdateUserRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UpdateUserRequestBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public UpdateUserRequestBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public UpdateUserRequest build() {
            return new UpdateUserRequest(name, avatarUrl, timezone);
        }
    }
}
