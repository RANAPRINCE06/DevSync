package com.devsync.coding.intelligence.repository;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.intelligence.entity.PlatformAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformAchievementRepository extends JpaRepository<PlatformAchievement, UUID> {

    List<PlatformAchievement> findAllByPlatformAccountIdOrderByEarnedDateDesc(UUID platformAccountId);

    @Query("SELECT a FROM PlatformAchievement a JOIN a.platformAccount acc WHERE acc.user.id = :userId ORDER BY a.earnedDate DESC, a.createdAt DESC")
    List<PlatformAchievement> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM PlatformAchievement a JOIN a.platformAccount acc WHERE acc.user.id = :userId AND a.platform = :platform ORDER BY a.earnedDate DESC, a.createdAt DESC")
    List<PlatformAchievement> findAllByUserIdAndPlatform(@Param("userId") UUID userId, @Param("platform") CodingPlatform platform);

    Optional<PlatformAchievement> findByPlatformAccountIdAndAchievementId(UUID platformAccountId, String achievementId);
}
