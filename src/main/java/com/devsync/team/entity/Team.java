package com.devsync.team.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Team() {
    }

    public Team(UUID id, String name, String description, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
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

    public static TeamBuilder builder() {
        return new TeamBuilder();
    }

    public static class TeamBuilder {
        private UUID id;
        private String name;
        private String description;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public TeamBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TeamBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TeamBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public TeamBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TeamBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Team build() {
            return new Team(id, name, description, active, createdAt, updatedAt);
        }
    }
}
