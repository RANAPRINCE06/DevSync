package com.devsync.notification.web;

import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.notification.dto.CreateNotificationRequest;
import com.devsync.notification.dto.NotificationResponse;
import com.devsync.notification.entity.NotificationChannel;
import com.devsync.notification.entity.NotificationStatus;
import com.devsync.notification.entity.NotificationType;
import com.devsync.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("POST /api/v1/notifications - returns 201 Created")
    void createNotification_Returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.DAILY_REMINDER)
                .title("Submit Progress")
                .message("Please update your daily work")
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(notificationId)
                .userId(userId)
                .userName("Prince")
                .type(NotificationType.DAILY_REMINDER)
                .channel(NotificationChannel.IN_APP)
                .status(NotificationStatus.PENDING)
                .title("Submit Progress")
                .message("Please update your daily work")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(notificationService.createNotification(any(CreateNotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/notifications - validation failure returns 400 Bad Request")
    void createNotification_ValidationFailure_Returns400() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(null)
                .type(null)
                .title("")
                .message("")
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{id} - returns 200 OK")
    void getNotificationById_Returns200() throws Exception {
        UUID notificationId = UUID.randomUUID();
        NotificationResponse response = NotificationResponse.builder()
                .id(notificationId)
                .title("Submit Progress")
                .status(NotificationStatus.PENDING)
                .build();

        when(notificationService.getNotificationById(notificationId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notificationId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{id} - not found returns 404")
    void getNotificationById_NotFound_Returns404() throws Exception {
        UUID notificationId = UUID.randomUUID();
        when(notificationService.getNotificationById(notificationId)).thenThrow(new ResourceNotFoundException("Notification not found"));

        mockMvc.perform(get("/api/v1/notifications/{id}", notificationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/{id}/read - returns 200 OK")
    void markAsRead_Returns200() throws Exception {
        UUID notificationId = UUID.randomUUID();
        NotificationResponse response = NotificationResponse.builder()
                .id(notificationId)
                .title("Submit Progress")
                .status(NotificationStatus.READ)
                .readAt(Instant.now())
                .build();

        when(notificationService.markAsRead(notificationId)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("READ"));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/read-all - returns 200 OK")
    void markAllAsRead_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(notificationService.markAllAsRead(userId)).thenReturn(5);

        mockMvc.perform(patch("/api/v1/notifications/read-all?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.updatedCount").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count - returns 200 OK")
    void getUnreadCount_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(notificationService.getUnreadCount(userId)).thenReturn(3L);

        mockMvc.perform(get("/api/v1/notifications/unread-count?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} - soft deletes returns 200 OK")
    void deleteNotification_Returns200() throws Exception {
        UUID notificationId = UUID.randomUUID();
        doNothing().when(notificationService).deleteNotification(notificationId);

        mockMvc.perform(delete("/api/v1/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - returns 200 OK paginated list")
    void getNotifications_Returns200() throws Exception {
        Page<NotificationResponse> page = new PageImpl<>(List.of());
        when(notificationService.getUserNotifications(any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
