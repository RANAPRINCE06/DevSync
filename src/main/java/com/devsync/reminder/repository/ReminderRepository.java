package com.devsync.reminder.repository;

import com.devsync.reminder.entity.Reminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID>, JpaSpecificationExecutor<Reminder> {

    List<Reminder> findByUserId(UUID userId);

    Page<Reminder> findByUserId(UUID userId, Pageable pageable);

    List<Reminder> findByTeamId(UUID teamId);

    Page<Reminder> findByTeamId(UUID teamId, Pageable pageable);

    List<Reminder> findByUserIdAndActive(UUID userId, boolean active);

    Page<Reminder> findByUserIdAndActive(UUID userId, boolean active, Pageable pageable);

    List<Reminder> findByActiveTrue();
}
