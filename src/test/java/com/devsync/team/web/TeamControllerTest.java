package com.devsync.team.web;

import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.team.dto.CreateTeamRequest;
import com.devsync.team.dto.TeamMemberResponse;
import com.devsync.team.dto.TeamResponse;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@Import(GlobalExceptionHandler.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;

    @Test
    @DisplayName("POST /api/v1/teams - returns 201 Created")
    void createTeam_Returns201() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("DevSync Squad")
                .description("Developer accountability team")
                .creatorUserId(userId)
                .build();

        TeamResponse response = TeamResponse.builder()
                .id(teamId)
                .name("DevSync Squad")
                .description("Developer accountability team")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(teamService.createTeam(any(CreateTeamRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(teamId.toString()))
                .andExpect(jsonPath("$.data.name").value("DevSync Squad"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/teams/{teamId}/members/{userId} - returns 201 Created")
    void addMember_Returns201() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        TeamMemberResponse response = TeamMemberResponse.builder()
                .id(memberId)
                .userId(userId)
                .userName("Prince")
                .userEmail("prince@example.com")
                .teamId(teamId)
                .teamName("DevSync Squad")
                .role(TeamRole.MEMBER)
                .joinedAt(Instant.now())
                .active(true)
                .build();

        when(teamService.addMember(teamId, userId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/teams/{id} - not found returns 404")
    void getTeamById_NotFound_Returns404() throws Exception {
        UUID teamId = UUID.randomUUID();
        when(teamService.getTeamById(teamId)).thenThrow(new ResourceNotFoundException("Team not found"));

        mockMvc.perform(get("/api/v1/teams/{id}", teamId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Team not found"));
    }
}
