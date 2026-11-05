package com.devsync.team.dto;

import com.devsync.team.entity.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class UpdateTeamMemberRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(example = "ADMIN", description = "New role for the team member (OWNER, ADMIN, MEMBER)")
    private TeamRole role;

    public UpdateTeamMemberRoleRequest() {
    }

    public UpdateTeamMemberRoleRequest(TeamRole role) {
        this.role = role;
    }

    public TeamRole getRole() {
        return role;
    }

    public void setRole(TeamRole role) {
        this.role = role;
    }

    public static UpdateTeamMemberRoleRequestBuilder builder() {
        return new UpdateTeamMemberRoleRequestBuilder();
    }

    public static class UpdateTeamMemberRoleRequestBuilder {
        private TeamRole role;

        public UpdateTeamMemberRoleRequestBuilder role(TeamRole role) {
            this.role = role;
            return this;
        }

        public UpdateTeamMemberRoleRequest build() {
            return new UpdateTeamMemberRoleRequest(role);
        }
    }
}

