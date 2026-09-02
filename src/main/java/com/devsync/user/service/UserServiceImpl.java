package com.devsync.user.service;

import com.devsync.auth.util.PasswordUtil;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.user.dto.CreateUserRequest;
import com.devsync.user.dto.UpdateUserRequest;
import com.devsync.user.dto.UserResponse;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String trimmedName = request.getName().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String timezone = request.getTimezone().trim();

        validateTimezone(timezone);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("User with email '" + normalizedEmail + "' already exists");
        }

        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            passwordHash = PasswordUtil.hashPassword(request.getPassword().trim());
        }

        User user = User.builder()
                .name(trimmedName)
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl().trim() : null)
                .timezone(timezone)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String timezone = request.getTimezone().trim();
        validateTimezone(timezone);

        user.setName(request.getName().trim());
        user.setAvatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl().trim() : null);
        user.setTimezone(timezone);

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    private void validateTimezone(String timezone) {
        if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
            throw new BadRequestException("Invalid IANA timezone: '" + timezone + "'. Example valid timezones: 'Asia/Kolkata', 'America/New_York', 'Europe/London'");
        }
    }
}
