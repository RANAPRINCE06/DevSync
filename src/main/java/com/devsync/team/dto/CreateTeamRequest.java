package com.devsync.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    @Schema(example = "DevSync Squad")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(example = "Four friends building and learning together")
    private String description;

    @NotNull(message = "Creator user ID is required")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID creatorUserId;

    public CreateTeamRequest() {
    }

    public CreateTeamRequest(String name, String description, UUID creatorUserId) {
        this.name = name;
        this.description = description;
        this.creatorUserId = creatorUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(UUID creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public static CreateTeamRequestBuilder builder() {
        return new CreateTeamRequestBuilder();
    }

    public static class CreateTeamRequestBuilder {
        private String name;
        private String description;
        private UUID creatorUserId;

        public CreateTeamRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CreateTeamRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateTeamRequestBuilder creatorUserId(UUID creatorUserId) {
            this.creatorUserId = creatorUserId;
            return this;
        }

        public CreateTeamRequest build() {
            return new CreateTeamRequest(name, description, creatorUserId);
        }
    }
}
