package com.devsync.goal.repository;

import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID>, JpaSpecificationExecutor<Goal> {

    @Query("SELECT g FROM Goal g WHERE g.id = :id AND g.owner.id = :ownerId")
    Optional<Goal> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    List<Goal> findByOwnerId(UUID ownerId);

    List<Goal> findByTeamId(UUID teamId);

    List<Goal> findByOwnerIdAndTeamId(UUID ownerId, UUID teamId);

    List<Goal> findByStatus(GoalStatus status);

    List<Goal> findByPriority(GoalPriority priority);

    Page<Goal> findByTeamIdAndActiveTrue(UUID teamId, Pageable pageable);
}
