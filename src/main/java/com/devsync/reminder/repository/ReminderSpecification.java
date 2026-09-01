package com.devsync.reminder.repository;

import com.devsync.reminder.entity.Reminder;
import com.devsync.reminder.entity.ReminderStatus;
import com.devsync.reminder.entity.ReminderType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class ReminderSpecification {

    public static Specification<Reminder> withUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Reminder> withTeamId(UUID teamId) {
        return (root, query, cb) -> teamId == null ? null : cb.equal(root.get("team").get("id"), teamId);
    }

    public static Specification<Reminder> withType(ReminderType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Reminder> withStatus(ReminderStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Reminder> withActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Reminder> withStartDateAfterOrEqual(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("startDate"), startDate);
    }

    public static Specification<Reminder> withEndDateBeforeOrEqual(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("endDate"), endDate);
    }

    public static Specification<Reminder> filter(UUID userId, UUID teamId, ReminderType type, ReminderStatus status, Boolean active, LocalDate startDate, LocalDate endDate) {
        return Specification.where(withUserId(userId))
                .and(withTeamId(teamId))
                .and(withType(type))
                .and(withStatus(status))
                .and(withActive(active))
                .and(withStartDateAfterOrEqual(startDate))
                .and(withEndDateBeforeOrEqual(endDate));
    }
}
