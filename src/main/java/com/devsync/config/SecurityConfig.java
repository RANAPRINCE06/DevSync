package com.devsync.config;

import com.devsync.auth.security.JwtAuthenticationFilter;
import com.devsync.auth.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security Configuration — Phase A2: Real JWT Authentication.
 *
 * <p>Changes from Phase A1:
 * <ul>
 *   <li>Removed the transitional {@code .requestMatchers("/api/v1/**").permitAll()} rule.</li>
 *   <li>All business API endpoints under {@code /api/v1/**} now require valid JWT authentication.</li>
 *   <li>{@link JwtAuthenticationFilter} is installed before
 *       {@link UsernamePasswordAuthenticationFilter} to populate the SecurityContext on every
 *       request that carries a valid Bearer token.</li>
 * </ul>
 *
 * <p>Public endpoints (no token required):
 * <ul>
 *   <li>{@code POST /api/v1/auth/login}</li>
 *   <li>{@code POST /api/v1/auth/register}</li>
 *   <li>{@code OPTIONS /**} — CORS preflight</li>
 *   <li>Swagger UI and OpenAPI documentation</li>
 *   <li>Actuator health and info</li>
 * </ul>
 *
 * <p>Phase A3 note: JWT establishes who the caller is. It does not yet verify that the caller
 * owns the requested resource (task, goal, team record, etc.). Object-level authorization
 * is implemented in Phase A3.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final DevSyncAuthenticationEntryPoint authenticationEntryPoint;
    private final DevSyncAccessDeniedHandler accessDeniedHandler;
    private final DevSyncPasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(DevSyncAuthenticationEntryPoint authenticationEntryPoint,
                          DevSyncAccessDeniedHandler accessDeniedHandler,
                          DevSyncPasswordEncoder passwordEncoder,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Stateless REST API — CSRF disabled (bearer-token architecture, no session cookies)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. CORS — aligned with React dev server ports
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Stateless session — no HttpSession created or stored
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Structured JSON error responses
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 5. HTTP Security Headers
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentTypeOptions(Customizer.withDefaults())
                        .cacheControl(Customizer.withDefaults())
                )

                // 6. Request Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // CORS Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public Authentication Routes
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()

                        // Swagger / OpenAPI Documentation
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Actuator — health and info are public; all others require ADMIN
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Administrative Endpoints — ADMIN role required
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // All remaining API endpoints require a valid, verified JWT
                        .anyRequest().authenticated()
                )

                // 7. Install JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
