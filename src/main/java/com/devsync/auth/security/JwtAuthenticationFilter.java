package com.devsync.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT Authentication Filter — executed once per request.
 *
 * <p>Processing flow:
 * <ol>
 *   <li>Skip public endpoints immediately (login, register, Swagger, actuator).</li>
 *   <li>Extract the Authorization header. Skip silently if absent or not Bearer-prefixed.</li>
 *   <li>Validate the JWT signature, algorithm, and expiration via {@link JwtTokenProvider}.</li>
 *   <li>On success: extract userId + roles from token claims and populate {@link SecurityContextHolder}.</li>
 *   <li>On any failure: clear context and continue the filter chain (endpoint-level security rejects
 *       the unauthenticated request with HTTP 401 via {@code DevSyncAuthenticationEntryPoint}).</li>
 * </ol>
 *
 * <p>Security invariants:
 * <ul>
 *   <li>A malformed, expired, or tampered JWT never populates the SecurityContext.</li>
 *   <li>Raw token values are never written to log output.</li>
 *   <li>User identity is always derived from the verified token — never from request parameters.</li>
 *   <li>The filter does not short-circuit the chain on failure; rejection is handled by security config.</li>
 * </ul>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Paths that never require JWT processing — avoids unnecessary parse overhead. */
    private static final List<String> PUBLIC_PATH_PATTERNS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/info"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return PUBLIC_PATH_PATTERNS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // All identity data comes from the verified token — never from the request
                UUID userId = jwtTokenProvider.extractUserId(token);
                String email = jwtTokenProvider.extractEmail(token);
                List<String> roles = jwtTokenProvider.extractRoles(token);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                // Principal is a DevSyncPrincipal carrying both UUID and email
                DevSyncPrincipal principal = new DevSyncPrincipal(userId, email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authentication established for user: {}", userId);
            }
        } catch (Exception ex) {
            // Clear context; let the filter chain proceed to endpoint security which returns 401.
            SecurityContextHolder.clearContext();
            log.warn("JWT filter: unexpected exception during token processing — context cleared");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw token string from the Authorization header.
     * Returns {@code null} if the header is absent or not Bearer-prefixed.
     * Accepts only "Bearer " prefix — raw token values are not logged.
     */
    private String resolveToken(HttpServletRequest request) {
        String headerValue = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerValue) && headerValue.startsWith(BEARER_PREFIX)) {
            return headerValue.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

