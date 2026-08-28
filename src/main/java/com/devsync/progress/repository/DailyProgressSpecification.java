package com.devsync.progress.repository;

import com.devsync.progress.entity.DailyProgress;
import com.devsync.progress.entity.ProgressStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class DailyProgressSpecification {

    public static Specification<DailyProgress> withUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<DailyProgress> withTeamId(UUID teamId) {
        return (root, query, cb) -> teamId == null ? null : cb.equal(root.get("team").get("id"), teamId);
    }

    public static Specification<DailyProgress> withExactDate(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.equal(root.get("progressDate"), date);
    }

    public static Specification<DailyProgress> withFromDate(LocalDate fromDate) {
        return (root, query, cb) -> fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("progressDate"), fromDate);
    }

    public static Specification<DailyProgress> withToDate(LocalDate toDate) {
        return (root, query, cb) -> toDate == null ? null : cb.lessThanOrEqualTo(root.get("progressDate"), toDate);
    }

    public static Specification<DailyProgress> withStatus(ProgressStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<DailyProgress> filter(
            UUID userId,
            UUID teamId,
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            ProgressStatus status) {
        return Specification.where(withUserId(userId))
                .and(withTeamId(teamId))
                .and(withExactDate(date))
                .and(withFromDate(fromDate))
                .and(withToDate(toDate))
                .and(withStatus(status));
    }
}
