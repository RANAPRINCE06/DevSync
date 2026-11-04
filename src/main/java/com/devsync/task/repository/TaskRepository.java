package com.devsync.task.repository;

import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.goal.owner.id = :ownerId")
    Optional<Task> findByIdAndGoalOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    List<Task> findByGoalId(UUID goalId);

    List<Task> findByAssigneeId(UUID assigneeId);

    List<Task> findByTeamId(UUID teamId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByGoalIdAndStatus(UUID goalId, TaskStatus status);
}
