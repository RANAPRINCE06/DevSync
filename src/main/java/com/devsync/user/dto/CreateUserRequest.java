package com.devsync.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(example = "Prince")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "prince@example.com")
    private String email;

    @Schema(example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @NotBlank(message = "Timezone is required")
    @Schema(example = "Asia/Kolkata")
    private String timezone;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String name, String email, String avatarUrl, String timezone) {
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.timezone = timezone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public static CreateUserRequestBuilder builder() {
        return new CreateUserRequestBuilder();
    }

    public static class CreateUserRequestBuilder {
        private String name;
        private String email;
        private String avatarUrl;
        private String timezone;

        public CreateUserRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CreateUserRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CreateUserRequestBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public CreateUserRequestBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public CreateUserRequest build() {
            return new CreateUserRequest(name, email, avatarUrl, timezone);
        }
    }
}
