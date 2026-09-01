package com.devsync.reminder.service;

import com.devsync.reminder.dto.CreateReminderRequest;
import com.devsync.reminder.dto.ReminderResponse;
import com.devsync.reminder.dto.UpdateReminderRequest;
import com.devsync.reminder.entity.ReminderStatus;
import com.devsync.reminder.entity.ReminderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface ReminderService {

    ReminderResponse createReminder(CreateReminderRequest request);

    ReminderResponse getReminderById(UUID id);

    ReminderResponse updateReminder(UUID id, UpdateReminderRequest request);

    void deactivateReminder(UUID id);

    Page<ReminderResponse> getReminders(UUID userId, UUID teamId, ReminderType type, ReminderStatus status, Boolean active, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
