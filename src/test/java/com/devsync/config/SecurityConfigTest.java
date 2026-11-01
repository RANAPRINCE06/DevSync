package com.devsync.config;

import com.devsync.auth.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Security configuration initializes and exposes required beans")
    void securityConfig_InitializesSuccessfully() {
        assertThat(applicationContext.getBean(SecurityFilterChain.class)).isNotNull();
        assertThat(applicationContext.getBean(DevSyncAuthenticationEntryPoint.class)).isNotNull();
        assertThat(applicationContext.getBean(DevSyncAccessDeniedHandler.class)).isNotNull();
        assertThat(passwordEncoder).isInstanceOf(DevSyncPasswordEncoder.class);
    }

    @Test
    @DisplayName("2. DevSyncPasswordEncoder correctly hashes and verifies passwords using PBKDF2")
    void passwordEncoder_HashesAndVerifiesPassword() {
        String rawPassword = "TestSecurePassword123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotNull();
        assertThat(encoded).contains(":");
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
        assertThat(passwordEncoder.matches("WrongPassword", encoded)).isFalse();
    }

    @Test
    @DisplayName("3. Public POST /api/v1/auth/login is accessible without authentication")
    void publicEndpoint_Login_IsAccessible() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("unregistered@devsync.io")
                .password("Password123!")
                .build();

        // Should reach AuthController and return 401 Unauthorized via business logic (user not found), not Spring Security filter block
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("4. Public POST /api/v1/auth/register is accessible without authentication")
    void publicEndpoint_Register_IsAccessible() throws Exception {
        // Empty body triggers bean validation in controller, proving endpoint is publicly reached
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("5. Public Actuator health endpoint is accessible without authentication")
    void publicEndpoint_ActuatorHealth_IsAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("6. Unauthorized request to protected endpoint returns structured HTTP 401 response")
    void unauthorizedRequest_ToProtectedEndpoint_ReturnsStructured401() throws Exception {
        mockMvc.perform(get("/api/v1/protected/account"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required to access this resource"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("7. Forbidden request from authenticated non-admin user returns structured HTTP 403 response")
    @WithMockUser(username = "standardUser", roles = {"USER"})
    void forbiddenRequest_ToAdminEndpoint_ReturnsStructured403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied: You do not have permission to access this resource"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("8. CORS preflight request from allowed origin returns 200 OK with proper headers")
    void corsPreflight_FromAllowedOrigin_Succeeds() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    @DisplayName("9. CORS preflight request from 127.0.0.1:5173 returns 200 OK")
    void corsPreflight_FromLoopbackOrigin_Succeeds() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://127.0.0.1:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5173"));
    }
}

