package com.devsync.achievement.web;

import com.devsync.achievement.dto.CreateAchievementRequest;
import com.devsync.achievement.dto.AchievementResponse;
import com.devsync.achievement.dto.UpdateAchievementRequest;
import com.devsync.achievement.entity.AchievementType;
import com.devsync.achievement.service.AchievementService;
import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AchievementController.class)
@Import(GlobalExceptionHandler.class)
class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AchievementService achievementService;

    @Test
    @DisplayName("POST /api/v1/achievements - returns 201 Created")
    void createAchievement_Returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID achievementId = UUID.randomUUID();

        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .userId(userId)
                .title("7-Day Streak")
                .type(AchievementType.STREAK)
                .points(100)
                .earnedAt(Instant.now())
                .build();

        AchievementResponse response = AchievementResponse.builder()
                .id(achievementId)
                .userId(userId)
                .userName("Prince")
                .title("7-Day Streak")
                .type(AchievementType.STREAK)
                .points(100)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(achievementService.createAchievement(any(CreateAchievementRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/achievements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(achievementId.toString()))
                .andExpect(jsonPath("$.data.title").value("7-Day Streak"));
    }

    @Test
    @DisplayName("POST /api/v1/achievements - validation failure returns 400 Bad Request")
    void createAchievement_ValidationFailure_Returns400() throws Exception {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .userId(null)
                .title("")
                .points(-5)
                .build();

        mockMvc.perform(post("/api/v1/achievements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/achievements/{id} - returns 200 OK")
    void getAchievementById_Returns200() throws Exception {
        UUID achievementId = UUID.randomUUID();
        AchievementResponse response = AchievementResponse.builder()
                .id(achievementId)
                .title("7-Day Streak")
                .active(true)
                .build();

        when(achievementService.getAchievementById(achievementId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/achievements/{id}", achievementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(achievementId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/achievements/{id} - not found returns 404")
    void getAchievementById_NotFound_Returns404() throws Exception {
        UUID achievementId = UUID.randomUUID();
        when(achievementService.getAchievementById(achievementId)).thenThrow(new ResourceNotFoundException("Achievement not found"));

        mockMvc.perform(get("/api/v1/achievements/{id}", achievementId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Achievement not found"));
    }

    @Test
    @DisplayName("PUT /api/v1/achievements/{id} - returns 200 OK")
    void updateAchievement_Returns200() throws Exception {
        UUID achievementId = UUID.randomUUID();
        UpdateAchievementRequest request = UpdateAchievementRequest.builder()
                .title("Updated Title")
                .type(AchievementType.STREAK)
                .points(150)
                .earnedAt(Instant.now())
                .active(true)
                .build();

        AchievementResponse response = AchievementResponse.builder()
                .id(achievementId)
                .title("Updated Title")
                .points(150)
                .active(true)
                .build();

        when(achievementService.updateAchievement(eq(achievementId), any(UpdateAchievementRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/achievements/{id}", achievementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    @DisplayName("DELETE /api/v1/achievements/{id} - deletes achievement returns 200 OK")
    void deleteAchievement_Returns200() throws Exception {
        UUID achievementId = UUID.randomUUID();
        doNothing().when(achievementService).deleteAchievement(achievementId);

        mockMvc.perform(delete("/api/v1/achievements/{id}", achievementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Achievement deleted successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/achievements - returns 200 OK paginated list")
    void getAchievements_Returns200() throws Exception {
        Page<AchievementResponse> page = new PageImpl<>(List.of());
        when(achievementService.getAchievements(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/achievements?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/achievements/user/{userId} - returns 200 OK")
    void getUserAchievements_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(achievementService.getUserAchievements(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/achievements/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/achievements/user/{userId}/points - returns 200 OK")
    void getUserTotalPoints_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(achievementService.getUserTotalPoints(userId)).thenReturn(500);

        mockMvc.perform(get("/api/v1/achievements/user/{userId}/points", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPoints").value(500));
    }
}
