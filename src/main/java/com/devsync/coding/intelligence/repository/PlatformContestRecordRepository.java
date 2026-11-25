package com.devsync.coding.intelligence.repository;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.intelligence.entity.PlatformContestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformContestRecordRepository extends JpaRepository<PlatformContestRecord, UUID> {

    List<PlatformContestRecord> findAllByPlatformAccountIdOrderByContestDateDesc(UUID platformAccountId);

    @Query("SELECT c FROM PlatformContestRecord c JOIN c.platformAccount acc WHERE acc.user.id = :userId ORDER BY c.contestDate DESC")
    List<PlatformContestRecord> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM PlatformContestRecord c JOIN c.platformAccount acc WHERE acc.user.id = :userId AND c.platform = :platform ORDER BY c.contestDate ASC")
    List<PlatformContestRecord> findAllByUserIdAndPlatformOrderByDateAsc(@Param("userId") UUID userId, @Param("platform") CodingPlatform platform);

    Optional<PlatformContestRecord> findByPlatformAccountIdAndContestId(UUID platformAccountId, String contestId);
}
