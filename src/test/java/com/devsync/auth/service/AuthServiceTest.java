package com.devsync.auth.service;

import com.devsync.auth.dto.AuthResponse;
import com.devsync.auth.dto.LoginRequest;
import com.devsync.auth.dto.RegisterRequest;
import com.devsync.auth.security.JwtTokenProvider;
import com.devsync.auth.util.PasswordUtil;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.common.exception.UnauthorizedException;
import com.devsync.user.dto.UserResponse;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private UUID userId;
    private String rawPassword;

    /** A realistic compact JWT stub — three dot-separated Base64url segments. */
    private static final String STUB_JWT =
            "eyJhbGciOiJIUzI1NiJ9" +
            ".eyJzdWIiOiJhbGV4QGRldnN5bmMuaW8ifQ" +
            ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        rawPassword = "SecurePassword123!";
        String hash = PasswordUtil.hashPassword(rawPassword);

        sampleUser = User.builder()
                .id(userId)
                .name("Alex Mercer")
                .email("alex@devsync.io")
                .passwordHash(hash)
                .timezone("UTC")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("login - success with valid email and password returns signed JWT")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("alex@devsync.io")
                .password(rawPassword)
                .build();

        when(userRepository.findByEmail("alex@devsync.io")).thenReturn(Optional.of(sampleUser));
        when(jwtTokenProvider.generateToken(eq(userId), eq("alex@devsync.io"), any())).thenReturn(STUB_JWT);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        // Phase A2: token is a proper JWT (three Base64url segments separated by dots)
        assertEquals(3, response.getToken().split("\\.").length,
                "Token must be a compact JWT with header.payload.signature structure");
        assertEquals("Bearer", response.getTokenType());
        assertEquals("Alex Mercer", response.getUser().getName());
        assertEquals("alex@devsync.io", response.getUser().getEmail());
        verify(jwtTokenProvider).generateToken(eq(userId), eq("alex@devsync.io"), any(List.class));
    }

    @Test
    @DisplayName("login - invalid password throws UnauthorizedException")
    void login_InvalidPassword_ThrowsUnauthorized() {
        LoginRequest request = LoginRequest.builder()
                .email("alex@devsync.io")
                .password("WrongPassword999")
                .build();

        when(userRepository.findByEmail("alex@devsync.io")).thenReturn(Optional.of(sampleUser));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("login - unknown email throws UnauthorizedException")
    void login_UnknownEmail_ThrowsUnauthorized() {
        LoginRequest request = LoginRequest.builder()
                .email("unknown@devsync.io")
                .password(rawPassword)
                .build();

        when(userRepository.findByEmail("unknown@devsync.io")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("login - deactivated user throws UnauthorizedException")
    void login_DeactivatedUser_ThrowsUnauthorized() {
        sampleUser.setActive(false);
        LoginRequest request = LoginRequest.builder()
                .email("alex@devsync.io")
                .password(rawPassword)
                .build();

        when(userRepository.findByEmail("alex@devsync.io")).thenReturn(Optional.of(sampleUser));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("register - success with valid details returns signed JWT")
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alex Mercer")
                .email("alex@devsync.io")
                .password(rawPassword)
                .timezone("UTC")
                .build();

        when(userRepository.existsByEmail("alex@devsync.io")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.generateToken(any(UUID.class), eq("alex@devsync.io"), any())).thenReturn(STUB_JWT);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(3, response.getToken().split("\\.").length,
                "Token must be a compact JWT with header.payload.signature structure");
        assertEquals("Alex Mercer", response.getUser().getName());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateToken(any(UUID.class), eq("alex@devsync.io"), any(List.class));
    }

    @Test
    @DisplayName("register - duplicate email throws BadRequestException; JWT never generated")
    void register_DuplicateEmail_ThrowsBadRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alex Mercer")
                .email("alex@devsync.io")
                .password(rawPassword)
                .timezone("UTC")
                .build();

        when(userRepository.existsByEmail("alex@devsync.io")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("getProfile - success")
    void getProfile_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        UserResponse response = authService.getProfile(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Alex Mercer", response.getName());
    }

    @Test
    @DisplayName("getProfile - not found throws ResourceNotFoundException")
    void getProfile_NotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getProfile(userId));
    }
}
