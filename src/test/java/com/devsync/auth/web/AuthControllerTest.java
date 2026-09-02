package com.devsync.auth.web;

import com.devsync.auth.dto.AuthResponse;
import com.devsync.auth.dto.LoginRequest;
import com.devsync.auth.dto.RegisterRequest;
import com.devsync.auth.service.AuthService;
import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.UnauthorizedException;
import com.devsync.user.dto.UserResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/login - valid credentials returns 200 OK")
    void login_ValidCredentials_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .name("Alex Mercer")
                .email("alex@devsync.io")
                .timezone("UTC")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        AuthResponse authResponse = AuthResponse.of(userResponse, "devsync_auth_token_123");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        LoginRequest request = LoginRequest.builder()
                .email("alex@devsync.io")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("devsync_auth_token_123"))
                .andExpect(jsonPath("$.data.user.name").value("Alex Mercer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - invalid credentials returns 401 Unauthorized")
    void login_InvalidCredentials_Returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        LoginRequest request = LoginRequest.builder()
                .email("alex@devsync.io")
                .password("WrongPass123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - validation failure with short password returns 400")
    void login_ShortPassword_Returns400() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("alex@devsync.io")
                .password("123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - returns 201 Created")
    void register_ValidRequest_Returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .name("Alex Mercer")
                .email("alex@devsync.io")
                .timezone("UTC")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        AuthResponse authResponse = AuthResponse.of(userResponse, "devsync_auth_token_123");

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        RegisterRequest request = RegisterRequest.builder()
                .name("Alex Mercer")
                .email("alex@devsync.io")
                .password("Password123!")
                .timezone("UTC")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("devsync_auth_token_123"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me/{userId} - returns 200 OK")
    void getProfile_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .name("Alex Mercer")
                .email("alex@devsync.io")
                .timezone("UTC")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(authService.getProfile(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()));
    }
}
