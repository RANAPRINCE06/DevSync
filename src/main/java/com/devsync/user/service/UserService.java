package com.devsync.user.service;

import com.devsync.user.dto.CreateUserRequest;
import com.devsync.user.dto.UpdateUserRequest;
import com.devsync.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID id);

    Page<UserResponse> getUsers(Pageable pageable);

    UserResponse updateUser(UUID id, UpdateUserRequest request);
}
