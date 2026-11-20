package com.devsync.coding.repository;

import com.devsync.coding.entity.PlatformStatSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformStatSnapshotRepository extends JpaRepository<PlatformStatSnapshot, UUID> {

    Page<PlatformStatSnapshot> findAllByPlatformAccountIdOrderBySnapshotAtDesc(UUID platformAccountId, Pageable pageable);

    Optional<PlatformStatSnapshot> findFirstByPlatformAccountIdOrderBySnapshotAtDesc(UUID platformAccountId);

    @Query("SELECT s FROM PlatformStatSnapshot s WHERE s.platformAccount.id IN :accountIds ORDER BY s.snapshotAt DESC")
    List<PlatformStatSnapshot> findAllByPlatformAccountIdInOrderBySnapshotAtDesc(@Param("accountIds") Collection<UUID> accountIds);
}
