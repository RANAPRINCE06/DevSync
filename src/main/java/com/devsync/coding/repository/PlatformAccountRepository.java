package com.devsync.coding.repository;

import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.enums.CodingPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformAccountRepository extends JpaRepository<PlatformAccount, UUID> {

    List<PlatformAccount> findAllByUserIdAndActiveTrueOrderByCreatedAtAsc(UUID userId);

    Optional<PlatformAccount> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    Optional<PlatformAccount> findByIdAndUserId(UUID id, UUID userId);

    Optional<PlatformAccount> findByUserIdAndPlatformAndActiveTrue(UUID userId, CodingPlatform platform);

    boolean existsByUserIdAndPlatformAndActiveTrue(UUID userId, CodingPlatform platform);
}
