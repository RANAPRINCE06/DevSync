package com.devsync.notification.reminder.repository;

import com.devsync.notification.reminder.entity.Reminder;
import com.devsync.notification.reminder.entity.ReminderType;
import org.springframework.data.jpa.domain.Specification;

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

    public static Specification<Reminder> withActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Reminder> filter(UUID userId, UUID teamId, ReminderType type, Boolean active) {
        return Specification.where(withUserId(userId))
                .and(withTeamId(teamId))
                .and(withType(type))
                .and(withActive(active));
    }
}
