package com.devsync.achievement.repository;

import com.devsync.achievement.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID>, JpaSpecificationExecutor<Achievement> {

    List<Achievement> findByUserId(UUID userId);

    List<Achievement> findByUserIdAndActive(UUID userId, boolean active);

    List<Achievement> findByUserIdAndActiveTrue(UUID userId);

    @Query("SELECT COALESCE(SUM(a.points), 0) FROM Achievement a WHERE a.user.id = :userId AND a.active = true")
    Integer sumPointsByUserIdAndActiveTrue(@Param("userId") UUID userId);
}
