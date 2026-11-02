package com.devsync.auth.security;

import com.devsync.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityService}.
 *
 * <p>Uses Mockito to stub {@link CurrentUserProvider} so each test exercises only the
 * ownership logic in {@code SecurityService} without touching the SecurityContextHolder.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityService")
class SecurityServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(currentUserProvider);
    }

    // =========================================================================
    // isCurrentUser
    // =========================================================================

    @Nested
    @DisplayName("isCurrentUser(UUID)")
    class IsCurrentUser {

        @Test
        @DisplayName("returns true when the userId matches the authenticated caller")
        void returnsTrue_WhenUserIdMatches() {
            UUID userId = UUID.randomUUID();
            when(currentUserProvider.isAuthenticated()).thenReturn(true);
            when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

            assertThat(securityService.isCurrentUser(userId)).isTrue();
        }

        @Test
        @DisplayName("returns false when the userId does not match the authenticated caller")
        void returnsFalse_WhenUserIdDoesNotMatch() {
            UUID callerId = UUID.randomUUID();
            UUID differentId = UUID.randomUUID();

            when(currentUserProvider.isAuthenticated()).thenReturn(true);
            when(currentUserProvider.getCurrentUserId()).thenReturn(callerId);

            assertThat(securityService.isCurrentUser(differentId)).isFalse();
        }

        @Test
        @DisplayName("returns false (without throwing) when the caller is unauthenticated")
        void returnsFalse_WhenUnauthenticated() {
            when(currentUserProvider.isAuthenticated()).thenReturn(false);

            // Must return false, not throw, so @PreAuthorize evaluates cleanly
            assertThat(securityService.isCurrentUser(UUID.randomUUID())).isFalse();
        }

        @Test
        @DisplayName("returns false when userId argument is null")
        void returnsFalse_WhenUserIdIsNull() {
            UUID callerId = UUID.randomUUID();
            when(currentUserProvider.isAuthenticated()).thenReturn(true);
            when(currentUserProvider.getCurrentUserId()).thenReturn(callerId);

            // null userId can never equal a real caller UUID
            assertThat(securityService.isCurrentUser(null)).isFalse();
        }
    }

    // =========================================================================
    // getCurrentUserId — delegation
    // =========================================================================

    @Nested
    @DisplayName("getCurrentUserId()")
    class GetCurrentUserId {

        @Test
        @DisplayName("returns authenticated user's UUID by delegating to CurrentUserProvider")
        void returnsAuthenticatedUserId() {
            UUID expected = UUID.randomUUID();
            when(currentUserProvider.getCurrentUserId()).thenReturn(expected);

            UUID result = securityService.getCurrentUserId();

            assertThat(result).isEqualTo(expected);
            verify(currentUserProvider).getCurrentUserId();
        }

        @Test
        @DisplayName("propagates UnauthorizedException from CurrentUserProvider when unauthenticated")
        void propagatesException_WhenUnauthenticated() {
            when(currentUserProvider.getCurrentUserId())
                    .thenThrow(new UnauthorizedException("Authentication required"));

            assertThatThrownBy(() -> securityService.getCurrentUserId())
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Authentication required");
        }
    }

    // =========================================================================
    // isAuthenticated — delegation
    // =========================================================================

    @Nested
    @DisplayName("isAuthenticated()")
    class IsAuthenticated {

        @Test
        @DisplayName("returns true when CurrentUserProvider reports authenticated")
        void returnsTrue_WhenAuthenticated() {
            when(currentUserProvider.isAuthenticated()).thenReturn(true);

            assertThat(securityService.isAuthenticated()).isTrue();
            verify(currentUserProvider).isAuthenticated();
        }

        @Test
        @DisplayName("returns false when CurrentUserProvider reports not authenticated")
        void returnsFalse_WhenNotAuthenticated() {
            when(currentUserProvider.isAuthenticated()).thenReturn(false);

            assertThat(securityService.isAuthenticated()).isFalse();
            verify(currentUserProvider).isAuthenticated();
        }
    }
}

