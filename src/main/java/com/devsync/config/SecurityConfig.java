package com.devsync.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security Foundation Configuration (Phase A1).
 *
 * Establishes a stateless REST architecture with custom entry points, CORS,
 * secure HTTP headers, and explicit route definitions.
 *
 * TRANSITIONAL ARCHITECTURE NOTICE:
 * In Phase A1, tokens are pseudo-bearer strings ('devsync_auth_*'). No JWT verification
 * filter exists yet (scheduled for Phase A2). To preserve backward compatibility and avoid
 * breaking current frontend functionality, existing business APIs under '/api/v1/**' are
 * accessible during this transition, while explicitly defined public routes, administrative
 * routes, and protected security test boundaries are strictly enforced.
 */
@AutoConfiguration
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final DevSyncAuthenticationEntryPoint authenticationEntryPoint;
    private final DevSyncAccessDeniedHandler accessDeniedHandler;
    private final DevSyncPasswordEncoder passwordEncoder;

    public SecurityConfig(DevSyncAuthenticationEntryPoint authenticationEntryPoint,
                          DevSyncAccessDeniedHandler accessDeniedHandler,
                          DevSyncPasswordEncoder passwordEncoder) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Stateless REST API - Disable CSRF (Stateless Bearer Tokens, no session cookies)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Cross-Origin Resource Sharing (CORS) Integration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Stateless Session Management (No HttpSession stored on server)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Custom Security Exception Handling (Structured JSON responses)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 5. Standard HTTP Security Headers
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentTypeOptions(Customizer.withDefaults())
                        .cacheControl(Customizer.withDefaults())
                )

                // 6. Request Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // CORS Preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public Authentication Routes
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()

                        // Public API Documentation & Swagger UI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Actuator Health & Info
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Explicitly Protected Administrative Endpoints (RBAC)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Explicitly Protected Boundary Endpoints (Phase A1 Security Enforcement)
                        .requestMatchers("/api/v1/protected/**").authenticated()

                        // Transitional API Routes: Accessible in Phase A1 until Phase A2 installs JwtAuthenticationFilter
                        .requestMatchers("/api/v1/**").permitAll()

                        // Default Fallback: Any other unmapped route requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
