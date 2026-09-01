package com.devsync.codingprofile.web;

import com.devsync.codingprofile.dto.CreateCodingProfileRequest;
import com.devsync.codingprofile.dto.CodingProfileResponse;
import com.devsync.codingprofile.dto.UpdateCodingProfileRequest;
import com.devsync.codingprofile.entity.CodingPlatform;
import com.devsync.codingprofile.service.CodingProfileService;
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

@WebMvcTest(CodingProfileController.class)
@Import(GlobalExceptionHandler.class)
class CodingProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CodingProfileService codingProfileService;

    @Test
    @DisplayName("POST /api/v1/coding-profiles - returns 201 Created")
    void createCodingProfile_Returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        CreateCodingProfileRequest request = CreateCodingProfileRequest.builder()
                .userId(userId)
                .platform(CodingPlatform.LEETCODE)
                .username("ranaprince06")
                .rating(1850)
                .problemsSolved(450)
                .build();

        CodingProfileResponse response = CodingProfileResponse.builder()
                .id(profileId)
                .userId(userId)
                .userName("Prince")
                .platform(CodingPlatform.LEETCODE)
                .username("ranaprince06")
                .rating(1850)
                .problemsSolved(450)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(codingProfileService.createCodingProfile(any(CreateCodingProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/coding-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(profileId.toString()))
                .andExpect(jsonPath("$.data.platform").value("LEETCODE"));
    }

    @Test
    @DisplayName("POST /api/v1/coding-profiles - validation failure returns 400 Bad Request")
    void createCodingProfile_ValidationFailure_Returns400() throws Exception {
        CreateCodingProfileRequest request = CreateCodingProfileRequest.builder()
                .userId(null)
                .platform(null)
                .username("")
                .rating(-10)
                .build();

        mockMvc.perform(post("/api/v1/coding-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/coding-profiles/{id} - returns 200 OK")
    void getCodingProfileById_Returns200() throws Exception {
        UUID profileId = UUID.randomUUID();
        CodingProfileResponse response = CodingProfileResponse.builder()
                .id(profileId)
                .platform(CodingPlatform.LEETCODE)
                .username("ranaprince06")
                .active(true)
                .build();

        when(codingProfileService.getCodingProfileById(profileId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/coding-profiles/{id}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(profileId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/coding-profiles/{id} - not found returns 404")
    void getCodingProfileById_NotFound_Returns404() throws Exception {
        UUID profileId = UUID.randomUUID();
        when(codingProfileService.getCodingProfileById(profileId)).thenThrow(new ResourceNotFoundException("Coding profile not found"));

        mockMvc.perform(get("/api/v1/coding-profiles/{id}", profileId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Coding profile not found"));
    }

    @Test
    @DisplayName("PUT /api/v1/coding-profiles/{id} - returns 200 OK")
    void updateCodingProfile_Returns200() throws Exception {
        UUID profileId = UUID.randomUUID();
        UpdateCodingProfileRequest request = UpdateCodingProfileRequest.builder()
                .username("new_username")
                .rating(1900)
                .active(true)
                .build();

        CodingProfileResponse response = CodingProfileResponse.builder()
                .id(profileId)
                .platform(CodingPlatform.LEETCODE)
                .username("new_username")
                .rating(1900)
                .active(true)
                .build();

        when(codingProfileService.updateCodingProfile(eq(profileId), any(UpdateCodingProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/coding-profiles/{id}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("new_username"));
    }

    @Test
    @DisplayName("DELETE /api/v1/coding-profiles/{id} - deletes profile returns 200 OK")
    void deleteCodingProfile_Returns200() throws Exception {
        UUID profileId = UUID.randomUUID();
        doNothing().when(codingProfileService).deleteCodingProfile(profileId);

        mockMvc.perform(delete("/api/v1/coding-profiles/{id}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Coding profile deleted successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/coding-profiles - returns 200 OK paginated list")
    void getCodingProfiles_Returns200() throws Exception {
        Page<CodingProfileResponse> page = new PageImpl<>(List.of());
        when(codingProfileService.getCodingProfiles(any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/coding-profiles?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/coding-profiles/user/{userId} - returns 200 OK")
    void getUserCodingProfiles_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(codingProfileService.getUserCodingProfiles(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/coding-profiles/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
