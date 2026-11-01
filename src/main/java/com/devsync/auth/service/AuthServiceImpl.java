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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Authentication service implementation (Phase A2).
 *
 * <p>Changes from Phase A1:
 * <ul>
 *   <li>Login and registration now generate a genuine HMAC-SHA256 signed JWT via
 *       {@link JwtTokenProvider} instead of the previous pseudo-token string.</li>
 *   <li>All password operations continue to use the existing PBKDF2-SHA256 implementation
 *       in {@link PasswordUtil}, preserving 100% backward compatibility with stored hashes.</li>
 *   <li>The {@code JwtTokenProvider} dependency is constructor-injected for testability.</li>
 * </ul>
 *
 * <p>Role assignment: the current schema has no role column on the {@code users} table.
 * All users are assigned the {@code "USER"} role at token generation time.
 * An ADMIN role claim will be introduced in Phase A3 when the role column is added.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final List<String> DEFAULT_USER_ROLES = List.of("USER");

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new UnauthorizedException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new UnauthorizedException("Password is required");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String rawPassword = request.getPassword().trim();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("This account has been deactivated. Please contact your team administrator.");
        }

        // Verify PBKDF2 password hash — compatible with all previously stored hashes.
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            boolean matches = PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash());
            if (!matches) {
                throw new UnauthorizedException("Invalid email or password");
            }
        } else {
            // Legacy user without password hash — set hash on first successful login.
            user.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
            userRepository.save(user);
        }

        // Generate a genuine signed JWT using the verified user identity from the database.
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), DEFAULT_USER_ROLES);
        return AuthResponse.of(UserResponse.fromEntity(user), token);
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String trimmedName = request.getName().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String timezone = request.getTimezone() != null && !request.getTimezone().trim().isEmpty()
                ? request.getTimezone().trim()
                : "UTC";

        validateTimezone(timezone);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("User with email '" + normalizedEmail + "' already exists");
        }

        String passwordHash = PasswordUtil.hashPassword(request.getPassword().trim());

        User user = User.builder()
                .name(trimmedName)
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl().trim() : null)
                .timezone(timezone)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // Generate a genuine signed JWT for the newly registered user.
        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), DEFAULT_USER_ROLES);
        return AuthResponse.of(UserResponse.fromEntity(savedUser), token);
    }

    // -------------------------------------------------------------------------
    // Profile retrieval
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserResponse.fromEntity(user);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void validateTimezone(String timezone) {
        if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
            throw new BadRequestException("Invalid IANA timezone: '" + timezone +
                    "'. Example valid timezones: 'Asia/Kolkata', 'America/New_York', 'Europe/London'");
        }
    }
}
