package com.devsync.auth.dto;

import com.devsync.user.dto.UserResponse;

public class AuthResponse {

    private UserResponse user;
    private String token;
    private String tokenType = "Bearer";

    public AuthResponse() {
    }

    public AuthResponse(UserResponse user, String token) {
        this.user = user;
        this.token = token;
        this.tokenType = "Bearer";
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public static AuthResponse of(UserResponse user, String token) {
        return new AuthResponse(user, token);
    }
}
