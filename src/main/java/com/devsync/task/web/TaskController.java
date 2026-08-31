package com.devsync.task.web;

import com.devsync.common.response.ApiResponse;
import com.devsync.task.dto.CreateTaskRequest;
import com.devsync.task.dto.TaskResponse;
import com.devsync.task.dto.UpdateTaskRequest;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import com.devsync.task.service.TaskService;
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
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Management", description = "Endpoints for creating, retrieving, updating, soft-deleting, and filtering goal tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a task", description = "Create an actionable task under an active goal")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a task by its UUID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable UUID id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Update task title, description, priority, due date, status, or actual minutes")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft delete a task", description = "Deactivate a task by setting active to false")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        taskService.deactivateTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deactivated successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get paginated tasks", description = "Filter tasks by goal, assignee, team, status, priority, due date, or active state with pagination")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasks(
            @RequestParam(required = false) @Parameter(description = "Filter by Goal UUID") UUID goalId,
            @RequestParam(required = false) @Parameter(description = "Filter by Assignee UUID") UUID assigneeId,
            @RequestParam(required = false) @Parameter(description = "Filter by Team UUID") UUID teamId,
            @RequestParam(required = false) @Parameter(description = "Filter by task status") TaskStatus status,
            @RequestParam(required = false) @Parameter(description = "Filter by task priority") TaskPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Filter tasks by exact due date (YYYY-MM-DD)", example = "2026-09-15") LocalDate dueDate,
            @RequestParam(required = false) @Parameter(description = "Filter by active state (default true)") Boolean active,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<TaskResponse> page = taskService.getTasks(goalId, assigneeId, teamId, status, priority, dueDate, active, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
