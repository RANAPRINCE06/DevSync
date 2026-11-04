package com.devsync.progress.repository;

import com.devsync.progress.entity.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyProgressRepository extends JpaRepository<DailyProgress, UUID>, JpaSpecificationExecutor<DailyProgress> {

    @Query("SELECT p FROM DailyProgress p WHERE p.id = :id AND p.user.id = :userId")
    Optional<DailyProgress> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    boolean existsByUserIdAndTeamIdAndProgressDate(UUID userId, UUID teamId, LocalDate progressDate);

    Optional<DailyProgress> findByUserIdAndTeamIdAndProgressDate(UUID userId, UUID teamId, LocalDate progressDate);
}
