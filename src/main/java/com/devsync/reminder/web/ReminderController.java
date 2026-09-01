package com.devsync.reminder.web;

import com.devsync.common.response.ApiResponse;
import com.devsync.reminder.dto.CreateReminderRequest;
import com.devsync.reminder.dto.ReminderResponse;
import com.devsync.reminder.dto.UpdateReminderRequest;
import com.devsync.reminder.entity.ReminderStatus;
import com.devsync.reminder.entity.ReminderType;
import com.devsync.reminder.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reminders")
@Tag(name = "Reminder Management", description = "Endpoints for configuring, updating, and querying scheduled reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    @Operation(summary = "Create a reminder", description = "Configure a scheduled reminder for a user and team")
    public ResponseEntity<ApiResponse<ReminderResponse>> createReminder(@Valid @RequestBody CreateReminderRequest request) {
        ReminderResponse response = reminderService.createReminder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reminder created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reminder by ID", description = "Retrieve reminder configuration details by UUID")
    public ResponseEntity<ApiResponse<ReminderResponse>> getReminderById(@PathVariable UUID id) {
        ReminderResponse response = reminderService.getReminderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reminder", description = "Update title, message, reminder time, timezone, dates, or status")
    public ResponseEntity<ApiResponse<ReminderResponse>> updateReminder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReminderRequest request) {
        ReminderResponse response = reminderService.updateReminder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Reminder updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a reminder", description = "Soft delete/deactivate a reminder (sets active=false, status=CANCELLED)")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable UUID id) {
        reminderService.deactivateReminder(id);
        return ResponseEntity.ok(ApiResponse.success("Reminder deactivated successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get paginated reminders", description = "Filter reminders by user, team, type, status, active state, and dates with pagination")
    public ResponseEntity<ApiResponse<Page<ReminderResponse>>> getReminders(
            @RequestParam(required = false) @Parameter(description = "Filter by User UUID") UUID userId,
            @RequestParam(required = false) @Parameter(description = "Filter by Team UUID") UUID teamId,
            @RequestParam(required = false) @Parameter(description = "Filter by reminder type") ReminderType type,
            @RequestParam(required = false) @Parameter(description = "Filter by reminder status") ReminderStatus status,
            @RequestParam(required = false) @Parameter(description = "Filter by active state") Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Filter start date after/equal (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Filter end date before/equal (YYYY-MM-DD)") LocalDate endDate,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ReminderResponse> page = reminderService.getReminders(userId, teamId, type, status, active, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
