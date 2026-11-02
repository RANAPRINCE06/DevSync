package com.devsync.auth.security;

import com.devsync.common.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CurrentUserProvider}.
 *
 * <p>Each test controls the {@link SecurityContextHolder} directly — no Spring context
 * is loaded, so tests are fast and isolated. The context is always cleared after each
 * test to prevent state leaking between cases.
 */
@DisplayName("CurrentUserProvider")
class CurrentUserProviderTest {

    private CurrentUserProvider provider;

    @BeforeEach
    void setUp() {
        provider = new CurrentUserProvider();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void setAuthenticatedPrincipal(DevSyncPrincipal principal) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private DevSyncPrincipal principalFor(UUID userId, String email) {
        return new DevSyncPrincipal(userId, email);
    }

    // =========================================================================
    // Authenticated — happy path
    // =========================================================================

    @Nested
    @DisplayName("When authenticated with a DevSyncPrincipal")
    class WhenAuthenticated {

        private UUID userId;
        private String email;

        @BeforeEach
        void authenticate() {
            userId = UUID.randomUUID();
            email = "user@devsync.io";
            setAuthenticatedPrincipal(principalFor(userId, email));
        }

        @Test
        @DisplayName("getCurrentUserId() returns the correct UUID")
        void getCurrentUserId_ReturnsCorrectUUID() {
            assertThat(provider.getCurrentUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("getCurrentUserEmail() returns the correct email")
        void getCurrentUserEmail_ReturnsCorrectEmail() {
            assertThat(provider.getCurrentUserEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("getCurrentPrincipal() returns the DevSyncPrincipal")
        void getCurrentPrincipal_ReturnsPrincipal() {
            DevSyncPrincipal result = provider.getCurrentPrincipal();
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("isAuthenticated() returns true")
        void isAuthenticated_ReturnsTrue() {
            assertThat(provider.isAuthenticated()).isTrue();
        }
    }

    // =========================================================================
    // Unauthenticated — empty context
    // =========================================================================

    @Nested
    @DisplayName("When security context is empty")
    class WhenContextEmpty {

        @Test
        @DisplayName("isAuthenticated() returns false")
        void isAuthenticated_ReturnsFalse() {
            // Context cleared in @AfterEach; nothing set here
            assertThat(provider.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("getCurrentUserId() throws UnauthorizedException")
        void getCurrentUserId_ThrowsUnauthorized() {
            assertThatThrownBy(() -> provider.getCurrentUserId())
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Authentication required");
        }

        @Test
        @DisplayName("getCurrentUserEmail() throws UnauthorizedException")
        void getCurrentUserEmail_ThrowsUnauthorized() {
            assertThatThrownBy(() -> provider.getCurrentUserEmail())
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Authentication required");
        }

        @Test
        @DisplayName("getCurrentPrincipal() throws UnauthorizedException")
        void getCurrentPrincipal_ThrowsUnauthorized() {
            assertThatThrownBy(() -> provider.getCurrentPrincipal())
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Authentication required");
        }
    }

    // =========================================================================
    // AnonymousAuthenticationToken
    // =========================================================================

    @Nested
    @DisplayName("When principal is an AnonymousAuthenticationToken")
    class WhenAnonymous {

        @BeforeEach
        void setAnonymous() {
            AnonymousAuthenticationToken anon = new AnonymousAuthenticationToken(
                    "anonymous-key",
                    "anonymousUser",
                    List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
            SecurityContextHolder.getContext().setAuthentication(anon);
        }

        @Test
        @DisplayName("isAuthenticated() returns false")
        void isAuthenticated_ReturnsFalse() {
            assertThat(provider.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("getCurrentUserId() throws UnauthorizedException")
        void getCurrentUserId_ThrowsUnauthorized() {
            assertThatThrownBy(() -> provider.getCurrentUserId())
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Authentication required");
        }
    }

    // =========================================================================
    // Non-DevSyncPrincipal principal
    // =========================================================================

    @Nested
    @DisplayName("When principal is not a DevSyncPrincipal")
    class WhenWrongPrincipalType {

        @BeforeEach
        void setStringPrincipal() {
            // A string "user" used as principal — as @WithMockUser does internally
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "plainStringPrincipal",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        @DisplayName("isAuthenticated() returns false (wrong principal type)")
        void isAuthenticated_ReturnsFalse() {
            assertThat(provider.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("getCurrentUserId() throws UnauthorizedException describing the type mismatch")
        void getCurrentUserId_ThrowsWithTypeMismatch() {
            assertThatThrownBy(() -> provider.getCurrentUserId())
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Authentication required");
        }

        @Test
        @DisplayName("getCurrentPrincipal() throws UnauthorizedException")
        void getCurrentPrincipal_ThrowsUnauthorized() {
            assertThatThrownBy(() -> provider.getCurrentPrincipal())
                    .isInstanceOf(UnauthorizedException.class);
        }
    }
}

