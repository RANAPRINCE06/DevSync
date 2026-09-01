package com.devsync.codingprofile.repository;

import com.devsync.codingprofile.entity.CodingPlatform;
import com.devsync.codingprofile.entity.CodingProfile;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CodingProfileSpecification {

    public static Specification<CodingProfile> withUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<CodingProfile> withPlatform(CodingPlatform platform) {
        return (root, query, cb) -> platform == null ? null : cb.equal(root.get("platform"), platform);
    }

    public static Specification<CodingProfile> withActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<CodingProfile> filter(UUID userId, CodingPlatform platform, Boolean active) {
        return Specification.where(withUserId(userId))
                .and(withPlatform(platform))
                .and(withActive(active));
    }
}
