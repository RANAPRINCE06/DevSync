package com.devsync.notification.repository;

import com.devsync.notification.entity.Notification;
import com.devsync.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status, Pageable pageable);

    List<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status);

    long countByUserIdAndStatus(UUID userId, NotificationStatus status);
}
