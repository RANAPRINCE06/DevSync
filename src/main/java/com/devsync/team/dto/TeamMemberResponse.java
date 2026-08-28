package com.devsync.team.dto;

import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;

import java.time.Instant;
import java.util.UUID;

public class TeamMemberResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID teamId;
    private String teamName;
    private TeamRole role;
    private Instant joinedAt;
    private boolean active;

    public TeamMemberResponse() {
    }

    public TeamMemberResponse(UUID id, UUID userId, String userName, String userEmail, UUID teamId, String teamName, TeamRole role, Instant joinedAt, boolean active) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.teamId = teamId;
        this.teamName = teamName;
        this.role = role;
        this.joinedAt = joinedAt;
        this.active = active;
    }

    public static TeamMemberResponse fromEntity(TeamMember member) {
        return TeamMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .active(member.isActive())
                .build();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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

    public static TeamMemberResponseBuilder builder() {
        return new TeamMemberResponseBuilder();
    }

    public static class TeamMemberResponseBuilder {
        private UUID id;
        private UUID userId;
        private String userName;
        private String userEmail;
        private UUID teamId;
        private String teamName;
        private TeamRole role;
        private Instant joinedAt;
        private boolean active;

        public TeamMemberResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TeamMemberResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public TeamMemberResponseBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public TeamMemberResponseBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public TeamMemberResponseBuilder teamId(UUID teamId) {
            this.teamId = teamId;
            return this;
        }

        public TeamMemberResponseBuilder teamName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public TeamMemberResponseBuilder role(TeamRole role) {
            this.role = role;
            return this;
        }

        public TeamMemberResponseBuilder joinedAt(Instant joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public TeamMemberResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public TeamMemberResponse build() {
            return new TeamMemberResponse(id, userId, userName, userEmail, teamId, teamName, role, joinedAt, active);
        }
    }
}
