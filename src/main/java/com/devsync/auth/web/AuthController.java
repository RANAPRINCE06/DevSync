package com.devsync.auth.web;

import com.devsync.auth.dto.AuthResponse;
import com.devsync.auth.dto.LoginRequest;
import com.devsync.auth.dto.RegisterRequest;
import com.devsync.auth.service.AuthService;
import com.devsync.common.response.ApiResponse;
import com.devsync.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & Session", description = "Endpoints for secure login, registration, and session verification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with Email & Password", description = "Authenticates credentials and returns a secure token and user profile")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account", description = "Creates a new user profile with password and returns session token")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account registered successfully", response));
    }

    @GetMapping("/me/{userId}")
    @Operation(summary = "Get current authenticated user profile", description = "Retrieves user details for the active session")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@PathVariable UUID userId) {
        UserResponse response = authService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
