package com.devsync.coding.repository;

import com.devsync.coding.entity.PlatformStatSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    @Query("SELECT s FROM PlatformStatSnapshot s WHERE s.platformAccount.id = :accountId " +
           "AND (:from IS NULL OR s.snapshotAt >= :from) " +
           "AND (:to IS NULL OR s.snapshotAt <= :to) " +
           "ORDER BY s.snapshotAt ASC")
    List<PlatformStatSnapshot> findByAccountIdAndDateRangeAsc(
            @Param("accountId") UUID accountId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("SELECT s FROM PlatformStatSnapshot s WHERE s.platformAccount.id IN :accountIds " +
           "AND (:from IS NULL OR s.snapshotAt >= :from) " +
           "AND (:to IS NULL OR s.snapshotAt <= :to) " +
           "ORDER BY s.snapshotAt ASC")
    List<PlatformStatSnapshot> findByAccountIdsAndDateRangeAsc(
            @Param("accountIds") Collection<UUID> accountIds,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("SELECT MIN(s.snapshotAt) FROM PlatformStatSnapshot s WHERE s.platformAccount.id IN :accountIds")
    Instant findEarliestSnapshotAtByAccountIds(@Param("accountIds") Collection<UUID> accountIds);

    @Query("SELECT MAX(s.snapshotAt) FROM PlatformStatSnapshot s WHERE s.platformAccount.id IN :accountIds")
    Instant findLatestSnapshotAtByAccountIds(@Param("accountIds") Collection<UUID> accountIds);

    @Query("SELECT COUNT(s) FROM PlatformStatSnapshot s WHERE s.platformAccount.id IN :accountIds")
    long countByAccountIds(@Param("accountIds") Collection<UUID> accountIds);

    @Query("SELECT MAX(s.currentRating) FROM PlatformStatSnapshot s WHERE s.platformAccount.id = :accountId")
    Double findMaxCurrentRatingByAccountId(@Param("accountId") UUID accountId);
}
