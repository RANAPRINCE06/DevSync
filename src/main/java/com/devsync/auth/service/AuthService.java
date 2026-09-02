package com.devsync.auth.service;

import com.devsync.auth.dto.AuthResponse;
import com.devsync.auth.dto.LoginRequest;
import com.devsync.auth.dto.RegisterRequest;
import com.devsync.user.dto.UserResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    UserResponse getProfile(UUID userId);
}
