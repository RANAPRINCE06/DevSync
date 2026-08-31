package com.devsync.goal.web;

import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.goal.dto.CreateGoalRequest;
import com.devsync.goal.dto.GoalResponse;
import com.devsync.goal.dto.UpdateGoalRequest;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import com.devsync.goal.service.GoalService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
@Import(GlobalExceptionHandler.class)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GoalService goalService;

    @Test
    @DisplayName("POST /api/v1/goals - returns 201 Created")
    void createGoal_Returns201() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(ownerId)
                .teamId(teamId)
                .title("Master Cloud Computing")
                .priority(GoalPriority.HIGH)
                .startDate(LocalDate.of(2026, 9, 1))
                .targetDate(LocalDate.of(2026, 12, 31))
                .build();

        GoalResponse response = GoalResponse.builder()
                .id(goalId)
                .ownerId(ownerId)
                .ownerName("Prince")
                .ownerEmail("prince@devsync.com")
                .teamId(teamId)
                .teamName("DevSync Team")
                .title("Master Cloud Computing")
                .status(GoalStatus.NOT_STARTED)
                .priority(GoalPriority.HIGH)
                .progressPercentage(0)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(goalService.createGoal(any(CreateGoalRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(goalId.toString()))
                .andExpect(jsonPath("$.data.title").value("Master Cloud Computing"))
                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"));
    }

    @Test
    @DisplayName("POST /api/v1/goals - validation failure returns 400 Bad Request")
    void createGoal_ValidationFailure_Returns400() throws Exception {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .ownerId(null)
                .teamId(null)
                .title("")
                .priority(null)
                .build();

        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/goals/{id} - returns 200 OK")
    void getGoalById_Returns200() throws Exception {
        UUID goalId = UUID.randomUUID();
        GoalResponse response = GoalResponse.builder()
                .id(goalId)
                .title("Master Cloud Computing")
                .status(GoalStatus.IN_PROGRESS)
                .build();

        when(goalService.getGoalById(goalId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/goals/{id}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(goalId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/goals/{id} - not found returns 404")
    void getGoalById_NotFound_Returns404() throws Exception {
        UUID goalId = UUID.randomUUID();
        when(goalService.getGoalById(goalId)).thenThrow(new ResourceNotFoundException("Goal not found"));

        mockMvc.perform(get("/api/v1/goals/{id}", goalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Goal not found"));
    }

    @Test
    @DisplayName("PUT /api/v1/goals/{id} - returns 200 OK")
    void updateGoal_Returns200() throws Exception {
        UUID goalId = UUID.randomUUID();
        UpdateGoalRequest request = UpdateGoalRequest.builder()
                .title("Master Cloud Computing Updated")
                .priority(GoalPriority.HIGH)
                .progressPercentage(50)
                .status(GoalStatus.IN_PROGRESS)
                .build();

        GoalResponse response = GoalResponse.builder()
                .id(goalId)
                .title("Master Cloud Computing Updated")
                .progressPercentage(50)
                .status(GoalStatus.IN_PROGRESS)
                .build();

        when(goalService.updateGoal(eq(goalId), any(UpdateGoalRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/goals/{id}", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Master Cloud Computing Updated"));
    }

    @Test
    @DisplayName("DELETE /api/v1/goals/{id} - soft deletes returns 200 OK")
    void deleteGoal_Returns200() throws Exception {
        UUID goalId = UUID.randomUUID();
        doNothing().when(goalService).deactivateGoal(goalId);

        mockMvc.perform(delete("/api/v1/goals/{id}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Goal deactivated successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/goals - returns 200 OK paginated list")
    void getGoals_Returns200() throws Exception {
        Page<GoalResponse> page = new PageImpl<>(List.of());
        when(goalService.getGoals(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/goals?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
