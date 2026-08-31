package com.devsync.notification.reminder.web;

import com.devsync.common.response.ApiResponse;
import com.devsync.notification.reminder.dto.CreateReminderRequest;
import com.devsync.notification.reminder.dto.ReminderResponse;
import com.devsync.notification.reminder.dto.UpdateReminderRequest;
import com.devsync.notification.reminder.entity.ReminderType;
import com.devsync.notification.reminder.service.ReminderService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reminders")
@Tag(name = "Reminder Management", description = "Endpoints for configuring and managing user and team scheduled reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    @Operation(summary = "Create a reminder", description = "Configure a daily or deadline reminder for a user or team")
    public ResponseEntity<ApiResponse<ReminderResponse>> createReminder(@Valid @RequestBody CreateReminderRequest request) {
        ReminderResponse response = reminderService.createReminder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reminder created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reminder by ID", description = "Retrieve a reminder configuration by its UUID")
    public ResponseEntity<ApiResponse<ReminderResponse>> getReminderById(@PathVariable UUID id) {
        ReminderResponse response = reminderService.getReminderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reminder", description = "Update title, message, reminder time, timezone, or active state")
    public ResponseEntity<ApiResponse<ReminderResponse>> updateReminder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReminderRequest request) {
        ReminderResponse response = reminderService.updateReminder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Reminder updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft delete a reminder", description = "Deactivate a reminder by setting active to false")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable UUID id) {
        reminderService.deactivateReminder(id);
        return ResponseEntity.ok(ApiResponse.success("Reminder deactivated successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get paginated reminders", description = "Filter reminders by user, team, type, or active state with pagination")
    public ResponseEntity<ApiResponse<Page<ReminderResponse>>> getReminders(
            @RequestParam(required = false) @Parameter(description = "Filter by User UUID") UUID userId,
            @RequestParam(required = false) @Parameter(description = "Filter by Team UUID") UUID teamId,
            @RequestParam(required = false) @Parameter(description = "Filter by reminder type") ReminderType type,
            @RequestParam(required = false) @Parameter(description = "Filter by active state") Boolean active,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ReminderResponse> page = reminderService.getUserReminders(userId, teamId, type, active, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
