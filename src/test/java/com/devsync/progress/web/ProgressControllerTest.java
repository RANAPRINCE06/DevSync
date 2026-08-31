package com.devsync.progress.web;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.progress.dto.CreateProgressRequest;
import com.devsync.progress.dto.ProgressResponse;
import com.devsync.progress.dto.UpdateProgressRequest;
import com.devsync.progress.entity.ProgressStatus;
import com.devsync.progress.service.ProgressService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
@Import(GlobalExceptionHandler.class)
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProgressService progressService;

    @Test
    @DisplayName("POST /api/v1/progress - returns 201 Created")
    void createProgress_Returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID progressId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        CreateProgressRequest request = CreateProgressRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .progressDate(today)
                .whatStudied("Spring Data JPA")
                .completed("Specifications implementation")
                .studyMinutes(120)
                .status(ProgressStatus.COMPLETED)
                .build();

        ProgressResponse response = ProgressResponse.builder()
                .id(progressId)
                .userId(userId)
                .userName("Prince")
                .userEmail("prince@devsync.com")
                .teamId(teamId)
                .teamName("DevSync Squad")
                .progressDate(today)
                .whatStudied("Spring Data JPA")
                .completed("Specifications implementation")
                .studyMinutes(120)
                .status(ProgressStatus.COMPLETED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(progressService.createProgress(any(CreateProgressRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(progressId.toString()))
                .andExpect(jsonPath("$.data.whatStudied").value("Spring Data JPA"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/v1/progress - validation failure returns 400 Bad Request")
    void createProgress_ValidationFailure_Returns400() throws Exception {
        CreateProgressRequest request = CreateProgressRequest.builder()
                .userId(null)
                .teamId(null)
                .progressDate(null)
                .whatStudied("")
                .completed("")
                .studyMinutes(2000) // Exceeds max 1440
                .status(null)
                .build();

        mockMvc.perform(post("/api/v1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/progress/{id} - returns 200 OK")
    void getProgressById_Returns200() throws Exception {
        UUID progressId = UUID.randomUUID();
        ProgressResponse response = ProgressResponse.builder()
                .id(progressId)
                .userName("Prince")
                .status(ProgressStatus.COMPLETED)
                .build();

        when(progressService.getProgressById(progressId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/progress/{id}", progressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(progressId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/progress/{id} - not found returns 404")
    void getProgressById_NotFound_Returns404() throws Exception {
        UUID progressId = UUID.randomUUID();
        when(progressService.getProgressById(progressId)).thenThrow(new ResourceNotFoundException("Progress entry not found"));

        mockMvc.perform(get("/api/v1/progress/{id}", progressId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Progress entry not found"));
    }

    @Test
    @DisplayName("PUT /api/v1/progress/{id} - returns 200 OK")
    void updateProgress_Returns200() throws Exception {
        UUID progressId = UUID.randomUUID();
        UpdateProgressRequest request = UpdateProgressRequest.builder()
                .whatStudied("Updated Study")
                .completed("Updated Tasks")
                .studyMinutes(150)
                .status(ProgressStatus.COMPLETED)
                .build();

        ProgressResponse response = ProgressResponse.builder()
                .id(progressId)
                .whatStudied("Updated Study")
                .completed("Updated Tasks")
                .studyMinutes(150)
                .status(ProgressStatus.COMPLETED)
                .build();

        when(progressService.updateProgress(eq(progressId), any(UpdateProgressRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/progress/{id}", progressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.whatStudied").value("Updated Study"));
    }

    @Test
    @DisplayName("GET /api/v1/progress - returns 200 OK paginated list")
    void getProgressList_Returns200() throws Exception {
        Page<ProgressResponse> page = new PageImpl<>(List.of());
        when(progressService.getProgressList(any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/progress?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
