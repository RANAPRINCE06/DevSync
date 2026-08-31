package com.devsync.notification.reminder.web;

import com.devsync.common.exception.GlobalExceptionHandler;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.notification.reminder.dto.CreateReminderRequest;
import com.devsync.notification.reminder.dto.ReminderResponse;
import com.devsync.notification.reminder.dto.UpdateReminderRequest;
import com.devsync.notification.reminder.entity.ReminderType;
import com.devsync.notification.reminder.service.ReminderService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReminderController.class)
@Import(GlobalExceptionHandler.class)
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReminderService reminderService;

    @Test
    @DisplayName("POST /api/v1/reminders - returns 201 Created")
    void createReminder_Returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID reminderId = UUID.randomUUID();

        CreateReminderRequest request = CreateReminderRequest.builder()
                .userId(userId)
                .type(ReminderType.DAILY_PROGRESS)
                .title("Submit Progress Reminder")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Asia/Kolkata")
                .build();

        ReminderResponse response = ReminderResponse.builder()
                .id(reminderId)
                .userId(userId)
                .userName("Prince")
                .type(ReminderType.DAILY_PROGRESS)
                .title("Submit Progress Reminder")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Asia/Kolkata")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(reminderService.createReminder(any(CreateReminderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(reminderId.toString()))
                .andExpect(jsonPath("$.data.title").value("Submit Progress Reminder"));
    }

    @Test
    @DisplayName("POST /api/v1/reminders - validation failure returns 400 Bad Request")
    void createReminder_ValidationFailure_Returns400() throws Exception {
        CreateReminderRequest request = CreateReminderRequest.builder()
                .userId(null)
                .type(null)
                .title("")
                .reminderTime(null)
                .timezone("")
                .build();

        mockMvc.perform(post("/api/v1/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/reminders/{id} - returns 200 OK")
    void getReminderById_Returns200() throws Exception {
        UUID reminderId = UUID.randomUUID();
        ReminderResponse response = ReminderResponse.builder()
                .id(reminderId)
                .title("Submit Progress Reminder")
                .active(true)
                .build();

        when(reminderService.getReminderById(reminderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reminders/{id}", reminderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(reminderId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/reminders/{id} - not found returns 404")
    void getReminderById_NotFound_Returns404() throws Exception {
        UUID reminderId = UUID.randomUUID();
        when(reminderService.getReminderById(reminderId)).thenThrow(new ResourceNotFoundException("Reminder not found"));

        mockMvc.perform(get("/api/v1/reminders/{id}", reminderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Reminder not found"));
    }

    @Test
    @DisplayName("PUT /api/v1/reminders/{id} - returns 200 OK")
    void updateReminder_Returns200() throws Exception {
        UUID reminderId = UUID.randomUUID();
        UpdateReminderRequest request = UpdateReminderRequest.builder()
                .title("Updated Reminder")
                .reminderTime(LocalTime.of(19, 0))
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        ReminderResponse response = ReminderResponse.builder()
                .id(reminderId)
                .title("Updated Reminder")
                .reminderTime(LocalTime.of(19, 0))
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        when(reminderService.updateReminder(eq(reminderId), any(UpdateReminderRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/reminders/{id}", reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Reminder"));
    }

    @Test
    @DisplayName("DELETE /api/v1/reminders/{id} - soft deletes returns 200 OK")
    void deleteReminder_Returns200() throws Exception {
        UUID reminderId = UUID.randomUUID();
        doNothing().when(reminderService).deactivateReminder(reminderId);

        mockMvc.perform(delete("/api/v1/reminders/{id}", reminderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reminder deactivated successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/reminders - returns 200 OK paginated list")
    void getReminders_Returns200() throws Exception {
        Page<ReminderResponse> page = new PageImpl<>(List.of());
        when(reminderService.getUserReminders(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/reminders?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
