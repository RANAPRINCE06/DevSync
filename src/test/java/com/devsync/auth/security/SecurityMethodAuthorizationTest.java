package com.devsync.auth.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying that the method-level security infrastructure
 * established in Phase A3.1 is correctly wired in the Spring application context.
 *
 * <p>Tests in this class:
 * <ol>
 *   <li>Confirm that {@code @EnableMethodSecurity} is active on {@code SecurityConfig}.</li>
 *   <li>Confirm that {@code SecurityService} is registered as bean {@code "securityService"}.</li>
 *   <li>Confirm that {@code CurrentUserProvider} is registered as a bean.</li>
 *   <li>Regression: public auth endpoints remain accessible without a JWT (Phase A2).</li>
 *   <li>Regression: protected endpoints return structured HTTP 401 without a JWT (Phase A2).</li>
 * </ol>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SecurityMethodAuthorization — infrastructure integration")
class SecurityMethodAuthorizationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // Bean / Configuration Verification
    // =========================================================================

    @Test
    @DisplayName("@EnableMethodSecurity annotation is present on SecurityConfig")
    void enableMethodSecurity_IsPresentOnSecurityConfig() {
        com.devsync.config.SecurityConfig securityConfig =
                applicationContext.getBean(com.devsync.config.SecurityConfig.class);

        // Spring wraps @Configuration beans in CGLIB proxies — use AnnotationUtils.findAnnotation
        // which traverses the proxy hierarchy and discovers annotations on the original class.
        EnableMethodSecurity annotation = AnnotationUtils.findAnnotation(
                securityConfig.getClass(), EnableMethodSecurity.class);

        assertThat(annotation)
                .as("SecurityConfig must be annotated with @EnableMethodSecurity")
                .isNotNull();
    }

    @Test
    @DisplayName("SecurityService is registered as bean 'securityService'")
    void securityService_IsRegisteredWithCorrectBeanName() {
        assertThat(applicationContext.containsBean("securityService"))
                .as("Spring bean named 'securityService' must exist for @PreAuthorize SpEL expressions")
                .isTrue();

        Object bean = applicationContext.getBean("securityService");
        assertThat(bean).isInstanceOf(SecurityService.class);
    }

    @Test
    @DisplayName("CurrentUserProvider is registered as a Spring bean")
    void currentUserProvider_IsRegisteredAsBean() {
        assertThat(applicationContext.containsBean("currentUserProvider"))
                .as("CurrentUserProvider must be a registered Spring bean")
                .isTrue();

        Object bean = applicationContext.getBean("currentUserProvider");
        assertThat(bean).isInstanceOf(CurrentUserProvider.class);
    }

    // =========================================================================
    // Regression: Phase A2 Endpoint Authorization Boundaries
    // =========================================================================

    @Test
    @DisplayName("[Regression] POST /api/v1/auth/login is publicly accessible without JWT")
    void loginEndpoint_RemainsPublicWithoutJwt() throws Exception {
        // Sending invalid credentials — expects 401 from business logic, not from security filter
        String body = "{\"email\":\"nobody@example.com\",\"password\":\"WrongPass123!\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("[Regression] Protected API route returns structured HTTP 401 without JWT")
    void protectedRoute_Returns401_WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required to access this resource"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
