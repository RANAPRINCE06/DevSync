package com.devsync.task.repository;

import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    List<Task> findByGoalId(UUID goalId);

    List<Task> findByAssigneeId(UUID assigneeId);

    List<Task> findByTeamId(UUID teamId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByGoalIdAndStatus(UUID goalId, TaskStatus status);
}
