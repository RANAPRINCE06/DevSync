package com.devsync.team.dto;

import com.devsync.team.entity.Team;

import java.time.Instant;
import java.util.UUID;

public class TeamResponse {

    private UUID id;
    private String name;
    private String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public TeamResponse() {
    }

    public TeamResponse(UUID id, String name, String description, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TeamResponse fromEntity(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .active(team.isActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static TeamResponseBuilder builder() {
        return new TeamResponseBuilder();
    }

    public static class TeamResponseBuilder {
        private UUID id;
        private String name;
        private String description;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;

        public TeamResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TeamResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TeamResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public TeamResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TeamResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TeamResponse build() {
            return new TeamResponse(id, name, description, active, createdAt, updatedAt);
        }
    }
}
