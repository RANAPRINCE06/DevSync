package com.devsync.auth.service;

import com.devsync.auth.dto.AuthResponse;
import com.devsync.auth.dto.LoginRequest;
import com.devsync.auth.dto.RegisterRequest;
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
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

        // Verify password hash
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            boolean matches = PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash());
            if (!matches) {
                throw new UnauthorizedException("Invalid email or password");
            }
        } else {
            // First time password setup for legacy user without password hash
            user.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
            userRepository.save(user);
        }

        String token = "devsync_auth_" + user.getId() + "_" + UUID.randomUUID().toString().replace("-", "");
        return AuthResponse.of(UserResponse.fromEntity(user), token);
    }

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
        String token = "devsync_auth_" + savedUser.getId() + "_" + UUID.randomUUID().toString().replace("-", "");
        return AuthResponse.of(UserResponse.fromEntity(savedUser), token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserResponse.fromEntity(user);
    }

    private void validateTimezone(String timezone) {
        if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
            throw new BadRequestException("Invalid IANA timezone: '" + timezone + "'. Example valid timezones: 'Asia/Kolkata', 'America/New_York', 'Europe/London'");
        }
    }
}
