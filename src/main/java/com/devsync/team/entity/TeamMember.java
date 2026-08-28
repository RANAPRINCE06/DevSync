package com.devsync.team.entity;

import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "team_members", uniqueConstraints = {
    @UniqueConstraint(name = "uk_team_members_user_team", columnNames = {"user_id", "team_id"})
})
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(nullable = false)
    private boolean active = true;

    public TeamMember() {
    }

    public TeamMember(UUID id, User user, Team team, TeamRole role, Instant joinedAt, boolean active) {
        this.id = id;
        this.user = user;
        this.team = team;
        this.role = role;
        this.joinedAt = joinedAt;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public TeamRole getRole() {
        return role;
    }

    public void setRole(TeamRole role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static TeamMemberBuilder builder() {
        return new TeamMemberBuilder();
    }

    public static class TeamMemberBuilder {
        private UUID id;
        private User user;
        private Team team;
        private TeamRole role;
        private Instant joinedAt;
        private boolean active = true;

        public TeamMemberBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TeamMemberBuilder user(User user) {
            this.user = user;
            return this;
        }

        public TeamMemberBuilder team(Team team) {
            this.team = team;
            return this;
        }

        public TeamMemberBuilder role(TeamRole role) {
            this.role = role;
            return this;
        }

        public TeamMemberBuilder joinedAt(Instant joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public TeamMemberBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public TeamMember build() {
            return new TeamMember(id, user, team, role, joinedAt, active);
        }
    }
}
