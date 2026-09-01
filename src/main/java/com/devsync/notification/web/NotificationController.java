package com.devsync.notification.web;

import com.devsync.common.response.ApiResponse;
import com.devsync.notification.dto.CreateNotificationRequest;
import com.devsync.notification.dto.NotificationResponse;
import com.devsync.notification.entity.NotificationStatus;
import com.devsync.notification.entity.NotificationType;
import com.devsync.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Management", description = "Endpoints for creating, viewing, marking, and deleting in-app notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create a notification", description = "Create an in-app notification for a user")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a notification by its UUID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(@PathVariable UUID id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get paginated notifications", description = "Filter notifications by user, type, and status with pagination")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @RequestParam(required = false) @Parameter(description = "Filter by User UUID") UUID userId,
            @RequestParam(required = false) @Parameter(description = "Filter by notification type") NotificationType type,
            @RequestParam(required = false) @Parameter(description = "Filter by notification status") NotificationStatus status,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<NotificationResponse> page = notificationService.getNotifications(userId, type, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve unread notifications for a user")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadNotifications(
            @RequestParam(required = false) @Parameter(description = "User UUID") UUID userId,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<NotificationResponse> page = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count", description = "Get the number of unread notifications for a user")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@RequestParam UUID userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Set notification status to READ and record read timestamp (idempotent)")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all unread notifications for a user as read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(@RequestParam UUID userId) {
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", Map.of("updatedCount", updatedCount)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification", description = "Delete a notification by UUID")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }
}
