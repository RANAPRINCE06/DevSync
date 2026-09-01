package com.devsync.leaderboard.web;

import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.leaderboard.dto.LeaderboardEntryResponse;
import com.devsync.leaderboard.entity.LeaderboardPeriod;
import com.devsync.leaderboard.service.LeaderboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaderboardController.class)
@Import(GlobalExceptionHandler.class)
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @Test
    @DisplayName("GET /api/v1/leaderboard/teams/{teamId} - returns 200 OK paginated")
    void getTeamLeaderboard_Returns200() throws Exception {
        UUID teamId = UUID.randomUUID();
        Page<LeaderboardEntryResponse> page = new PageImpl<>(List.of(
                LeaderboardEntryResponse.builder()
                        .rank(1)
                        .userId(UUID.randomUUID())
                        .userName("Alice")
                        .teamId(teamId)
                        .teamName("DevSync Team")
                        .score(180)
                        .build()
        ));

        when(leaderboardService.getTeamLeaderboard(eq(teamId), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/leaderboard/teams/{teamId}?period=ALL_TIME", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userName").value("Alice"))
                .andExpect(jsonPath("$.data.content[0].score").value(180));
    }

    @Test
    @DisplayName("GET /api/v1/leaderboard/teams/{teamId}/me/{userId} - returns 200 OK")
    void getUserRanking_Returns200() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        LeaderboardEntryResponse response = LeaderboardEntryResponse.builder()
                .rank(1)
                .userId(userId)
                .userName("Alice")
                .teamId(teamId)
                .teamName("DevSync Team")
                .score(180)
                .build();

        when(leaderboardService.getUserRanking(eq(teamId), eq(userId), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/leaderboard/teams/{teamId}/me/{userId}?period=ALL_TIME", teamId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rank").value(1))
                .andExpect(jsonPath("$.data.score").value(180));
    }

    @Test
    @DisplayName("GET /api/v1/leaderboard/teams/{teamId}/me/{userId} - non-member returns 404")
    void getUserRanking_NonMember_Returns404() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(leaderboardService.getUserRanking(eq(teamId), eq(userId), any()))
                .thenThrow(new ResourceNotFoundException("User not found in team leaderboard"));

        mockMvc.perform(get("/api/v1/leaderboard/teams/{teamId}/me/{userId}", teamId, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found in team leaderboard"));
    }

    @Test
    @DisplayName("GET /api/v1/leaderboard/teams/{teamId}/period/{period} - returns 200 OK")
    void getLeaderboardForPeriod_Returns200() throws Exception {
        UUID teamId = UUID.randomUUID();
        Page<LeaderboardEntryResponse> page = new PageImpl<>(List.of());

        when(leaderboardService.getLeaderboardForPeriod(eq(teamId), eq(LeaderboardPeriod.WEEKLY), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/leaderboard/teams/{teamId}/period/{period}", teamId, "WEEKLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
