package com.devsync.notification.reminder.service;

import com.devsync.notification.reminder.dto.CreateReminderRequest;
import com.devsync.notification.reminder.dto.ReminderResponse;
import com.devsync.notification.reminder.dto.UpdateReminderRequest;
import com.devsync.notification.reminder.entity.ReminderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReminderService {

    ReminderResponse createReminder(CreateReminderRequest request);

    ReminderResponse getReminderById(UUID id);

    ReminderResponse updateReminder(UUID id, UpdateReminderRequest request);

    void deactivateReminder(UUID id);

    Page<ReminderResponse> getUserReminders(UUID userId, UUID teamId, ReminderType type, Boolean active, Pageable pageable);
}
