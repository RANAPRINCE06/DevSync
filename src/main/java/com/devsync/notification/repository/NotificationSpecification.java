package com.devsync.notification.repository;

import com.devsync.notification.entity.Notification;
import com.devsync.notification.entity.NotificationStatus;
import com.devsync.notification.entity.NotificationType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class NotificationSpecification {

    public static Specification<Notification> withUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Notification> withType(NotificationType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Notification> withStatus(NotificationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Notification> filter(UUID userId, NotificationType type, NotificationStatus status) {
        return Specification.where(withUserId(userId))
                .and(withType(type))
                .and(withStatus(status));
    }
}
