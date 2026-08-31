package com.devsync.notification.reminder.repository;

import com.devsync.notification.reminder.entity.Reminder;
import com.devsync.notification.reminder.entity.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID>, JpaSpecificationExecutor<Reminder> {

    List<Reminder> findByUserId(UUID userId);

    List<Reminder> findByUserIdAndActive(UUID userId, boolean active);

    boolean existsByUserIdAndTypeAndTeamIdAndReminderTimeAndActiveTrue(UUID userId, ReminderType type, UUID teamId, LocalTime reminderTime);

    boolean existsByUserIdAndTypeAndTeamIsNullAndReminderTimeAndActiveTrue(UUID userId, ReminderType type, LocalTime reminderTime);
}
