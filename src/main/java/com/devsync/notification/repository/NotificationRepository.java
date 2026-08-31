package com.devsync.notification.repository;

import com.devsync.notification.entity.Notification;
import com.devsync.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    long countByUserIdAndStatusAndDeletedFalse(UUID userId, NotificationStatus status);

    long countByUserIdAndStatusInAndDeletedFalse(UUID userId, Collection<NotificationStatus> statuses);

    List<Notification> findByUserIdAndStatusAndDeletedFalse(UUID userId, NotificationStatus status);

    List<Notification> findByUserIdAndStatusInAndDeletedFalse(UUID userId, Collection<NotificationStatus> statuses);

    List<Notification> findByUserIdAndDeletedFalse(UUID userId);
}
