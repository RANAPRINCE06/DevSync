package com.devsync.achievement.repository;

import com.devsync.achievement.entity.Achievement;
import com.devsync.achievement.entity.AchievementType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class AchievementSpecification {

    public static Specification<Achievement> withUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Achievement> withType(AchievementType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Achievement> withActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Achievement> withEarnedAtAfterOrEqual(Instant earnedAtFrom) {
        return (root, query, cb) -> earnedAtFrom == null ? null : cb.greaterThanOrEqualTo(root.get("earnedAt"), earnedAtFrom);
    }

    public static Specification<Achievement> withEarnedAtBeforeOrEqual(Instant earnedAtTo) {
        return (root, query, cb) -> earnedAtTo == null ? null : cb.lessThanOrEqualTo(root.get("earnedAt"), earnedAtTo);
    }

    public static Specification<Achievement> filter(UUID userId, AchievementType type, Boolean active, Instant earnedAtFrom, Instant earnedAtTo) {
        return Specification.where(withUserId(userId))
                .and(withType(type))
                .and(withActive(active))
                .and(withEarnedAtAfterOrEqual(earnedAtFrom))
                .and(withEarnedAtBeforeOrEqual(earnedAtTo));
    }
}
