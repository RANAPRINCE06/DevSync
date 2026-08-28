package com.devsync.user.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.user.dto.CreateUserRequest;
import com.devsync.user.dto.UpdateUserRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = User.builder()
                .id(userId)
                .name("Prince")
                .email("prince@example.com")
                .avatarUrl("https://example.com/avatar.jpg")
                .timezone("Asia/Kolkata")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createUser - success")
    void createUser_Success() {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("  Prince  ")
                .email("Prince@Example.com ")
                .avatarUrl("https://example.com/avatar.jpg")
                .timezone("Asia/Kolkata")
                .build();

        when(userRepository.existsByEmail("prince@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("Prince", response.getName());
        assertEquals("prince@example.com", response.getEmail());
        assertTrue(response.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - duplicate email throws BadRequestException")
    void createUser_DuplicateEmail_ThrowsException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Prince")
                .email("prince@example.com")
                .timezone("Asia/Kolkata")
                .build();

        when(userRepository.existsByEmail("prince@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - invalid timezone throws BadRequestException")
    void createUser_InvalidTimezone_ThrowsException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Prince")
                .email("prince@example.com")
                .timezone("+05:30")
                .build();

        assertThrows(BadRequestException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getUserById - success")
    void getUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Prince", response.getName());
    }

    @Test
    @DisplayName("getUserById - not found throws ResourceNotFoundException")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    @DisplayName("getUsers - returns paginated users")
    void getUsers_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(sampleUser));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> result = userService.getUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Prince", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("updateUser - success")
    void updateUser_Success() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Prince Rana")
                .avatarUrl("https://example.com/new.jpg")
                .timezone("Europe/London")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.updateUser(userId, request);

        assertNotNull(response);
        verify(userRepository).save(sampleUser);
    }
}
