package com.devsync.task.web;

import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.task.dto.CreateTaskRequest;
import com.devsync.task.dto.TaskResponse;
import com.devsync.task.dto.UpdateTaskRequest;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import com.devsync.task.service.TaskService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    @DisplayName("POST /api/v1/tasks - returns 201 Created")
    void createTask_Returns201() throws Exception {
        UUID goalId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(goalId)
                .assigneeId(assigneeId)
                .title("Write JWT Security Tests")
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.of(2026, 9, 20))
                .estimatedMinutes(90)
                .build();

        TaskResponse response = TaskResponse.builder()
                .id(taskId)
                .goalId(goalId)
                .goalTitle("Master Spring Boot")
                .assigneeId(assigneeId)
                .assigneeName("Prince")
                .title("Write JWT Security Tests")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId.toString()))
                .andExpect(jsonPath("$.data.title").value("Write JWT Security Tests"))
                .andExpect(jsonPath("$.data.status").value("TODO"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks - validation failure returns 400 Bad Request")
    void createTask_ValidationFailure_Returns400() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .goalId(null)
                .assigneeId(null)
                .title("")
                .priority(null)
                .estimatedMinutes(2000) // Exceeds max 1440
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} - returns 200 OK")
    void getTaskById_Returns200() throws Exception {
        UUID taskId = UUID.randomUUID();
        TaskResponse response = TaskResponse.builder()
                .id(taskId)
                .title("Write JWT Security Tests")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskService.getTaskById(taskId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} - not found returns 404")
    void getTaskById_NotFound_Returns404() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.getTaskById(taskId)).thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    @DisplayName("PUT /api/v1/tasks/{id} - returns 200 OK")
    void updateTask_Returns200() throws Exception {
        UUID taskId = UUID.randomUUID();
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Write JWT Security Tests (Updated)")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.COMPLETED)
                .actualMinutes(95)
                .build();

        TaskResponse response = TaskResponse.builder()
                .id(taskId)
                .title("Write JWT Security Tests (Updated)")
                .status(TaskStatus.COMPLETED)
                .actualMinutes(95)
                .build();

        when(taskService.updateTask(eq(taskId), any(UpdateTaskRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id} - soft deletes returns 200 OK")
    void deleteTask_Returns200() throws Exception {
        UUID taskId = UUID.randomUUID();
        doNothing().when(taskService).deactivateTask(taskId);

        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task deactivated successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks - returns 200 OK paginated list")
    void getTasks_Returns200() throws Exception {
        Page<TaskResponse> page = new PageImpl<>(List.of());
        when(taskService.getTasks(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
