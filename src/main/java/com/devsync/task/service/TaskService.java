package com.devsync.task.service;

import com.devsync.task.dto.CreateTaskRequest;
import com.devsync.task.dto.TaskResponse;
import com.devsync.task.dto.UpdateTaskRequest;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse getTaskById(UUID id);

    TaskResponse updateTask(UUID id, UpdateTaskRequest request);

    void deactivateTask(UUID id);

    Page<TaskResponse> getTasks(
            UUID goalId,
            UUID assigneeId,
            UUID teamId,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Boolean active,
            Pageable pageable);
}
