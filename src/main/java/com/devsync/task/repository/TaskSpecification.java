package com.devsync.task.repository;

import com.devsync.task.entity.Task;
import com.devsync.task.entity.TaskPriority;
import com.devsync.task.entity.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> withGoalId(UUID goalId) {
        return (root, query, cb) -> goalId == null ? null : cb.equal(root.get("goal").get("id"), goalId);
    }

    public static Specification<Task> withAssigneeId(UUID assigneeId) {
        return (root, query, cb) -> assigneeId == null ? null : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task> withTeamId(UUID teamId) {
        return (root, query, cb) -> teamId == null ? null : cb.equal(root.get("team").get("id"), teamId);
    }

    public static Specification<Task> withStatus(TaskStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> withPriority(TaskPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> withDueDate(LocalDate dueDate) {
        return (root, query, cb) -> dueDate == null ? null : cb.equal(root.get("dueDate"), dueDate);
    }

    public static Specification<Task> withActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Task> withGoalOwnerId(UUID ownerId) {
        return (root, query, cb) -> ownerId == null ? null : cb.equal(root.get("goal").get("owner").get("id"), ownerId);
    }

    public static Specification<Task> withTeamTask(Boolean teamTask) {
        return (root, query, cb) -> teamTask == null ? null : cb.equal(root.get("teamTask"), teamTask);
    }

    public static Specification<Task> withVisibility(UUID currentUserId, UUID teamId) {
        return (root, query, cb) -> {
            if (teamId != null) {
                // In team context: team tasks OR caller's personal tasks in this team
                return cb.and(
                        cb.equal(root.get("team").get("id"), teamId),
                        cb.or(
                                cb.isTrue(root.get("teamTask")),
                                cb.and(cb.isFalse(root.get("teamTask")), cb.equal(root.get("goal").get("owner").get("id"), currentUserId))
                        )
                );
            } else {
                // Outside team context: caller's personal tasks only
                return cb.and(
                        cb.equal(root.get("goal").get("owner").get("id"), currentUserId),
                        cb.isFalse(root.get("teamTask"))
                );
            }
        };
    }

    public static Specification<Task> filter(
            UUID goalId,
            UUID assigneeId,
            UUID teamId,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Boolean active) {
        return filter(goalId, assigneeId, teamId, status, priority, dueDate, active, null);
    }

    public static Specification<Task> filter(
            UUID goalId,
            UUID assigneeId,
            UUID teamId,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Boolean active,
            UUID goalOwnerId) {
        return Specification.where(withGoalId(goalId))
                .and(withAssigneeId(assigneeId))
                .and(withTeamId(teamId))
                .and(withStatus(status))
                .and(withPriority(priority))
                .and(withDueDate(dueDate))
                .and(withActive(active))
                .and(withGoalOwnerId(goalOwnerId));
    }

    public static Specification<Task> filterWithVisibility(
            UUID currentUserId,
            UUID goalId,
            UUID assigneeId,
            UUID teamId,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Boolean active,
            Boolean teamTask) {
        return Specification.where(withVisibility(currentUserId, teamId))
                .and(withGoalId(goalId))
                .and(withAssigneeId(assigneeId))
                .and(withStatus(status))
                .and(withPriority(priority))
                .and(withDueDate(dueDate))
                .and(withActive(active))
                .and(withTeamTask(teamTask));
    }
}
