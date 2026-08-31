package com.devsync.goal.repository;

import com.devsync.goal.entity.Goal;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class GoalSpecification {

    public static Specification<Goal> withOwnerId(UUID ownerId) {
        return (root, query, cb) -> ownerId == null ? null : cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Goal> withTeamId(UUID teamId) {
        return (root, query, cb) -> teamId == null ? null : cb.equal(root.get("team").get("id"), teamId);
    }

    public static Specification<Goal> withStatus(GoalStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Goal> withPriority(GoalPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Goal> withActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Goal> withStartDateAfterOrEqual(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("startDate"), startDate);
    }

    public static Specification<Goal> withTargetDateBeforeOrEqual(LocalDate targetDate) {
        return (root, query, cb) -> targetDate == null ? null : cb.lessThanOrEqualTo(root.get("targetDate"), targetDate);
    }

    public static Specification<Goal> filter(
            UUID ownerId,
            UUID teamId,
            GoalStatus status,
            GoalPriority priority,
            Boolean active,
            LocalDate startDate,
            LocalDate targetDate) {
        return Specification.where(withOwnerId(ownerId))
                .and(withTeamId(teamId))
                .and(withStatus(status))
                .and(withPriority(priority))
                .and(withActive(active))
                .and(withStartDateAfterOrEqual(startDate))
                .and(withTargetDateBeforeOrEqual(targetDate));
    }
}
