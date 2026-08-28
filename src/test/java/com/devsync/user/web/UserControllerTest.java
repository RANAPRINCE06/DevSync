package com.devsync.user.web;

import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.user.dto.CreateUserRequest;
import com.devsync.user.dto.UserResponse;
import com.devsync.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/v1/users - returns 201 Created")
    void createUser_Returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Prince")
                .email("prince@example.com")
                .timezone("Asia/Kolkata")
                .build();

        UserResponse response = UserResponse.builder()
                .id(userId)
                .name("Prince")
                .email("prince@example.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.name").value("Prince"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/users - validation failure returns 400 Bad Request")
    void createUser_ValidationFailure_Returns400() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("")
                .email("invalid-email")
                .timezone("Asia/Kolkata")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - not found returns 404")
    void getUserById_NotFound_Returns404() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("GET /api/v1/users - default pagination page=0 size=10")
    void getUsers_DefaultPagination() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of());
        when(userService.getUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).getUsers(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Test
    @DisplayName("GET /api/v1/users?page=0&size=10&sort=name,asc - sorting by name asc")
    void getUsers_CustomSorting_NameAsc() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of());
        when(userService.getUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users?page=0&size=10&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).getUsers(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
    }

    @Test
    @DisplayName("GET /api/v1/users?page=0&size=10&sort=email,desc - sorting by email desc")
    void getUsers_CustomSorting_EmailDesc() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of());
        when(userService.getUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users?page=0&size=10&sort=email,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).getUsers(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "email")));
    }

    @Test
    @DisplayName("GET /api/v1/users - invalid sort property returns 400 Bad Request")
    void getUsers_InvalidSortProperty_Returns400() throws Exception {
        PropertyReferenceException prefEx = new PropertyReferenceException(
                "invalidField",
                TypeInformation.of(com.devsync.user.entity.User.class),
                List.of()
        );
        when(userService.getUsers(any(Pageable.class))).thenThrow(prefEx);

        mockMvc.perform(get("/api/v1/users?page=0&size=10&sort=invalidField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid sort property: 'invalidField'"));
    }
}
