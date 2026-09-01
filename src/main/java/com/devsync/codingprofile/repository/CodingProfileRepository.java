package com.devsync.codingprofile.repository;

import com.devsync.codingprofile.entity.CodingPlatform;
import com.devsync.codingprofile.entity.CodingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodingProfileRepository extends JpaRepository<CodingProfile, UUID>, JpaSpecificationExecutor<CodingProfile> {

    boolean existsByUserIdAndPlatformAndActiveTrue(UUID userId, CodingPlatform platform);

    List<CodingProfile> findByUserId(UUID userId);

    List<CodingProfile> findByUserIdAndActive(UUID userId, boolean active);

    Optional<CodingProfile> findByUserIdAndPlatformAndActiveTrue(UUID userId, CodingPlatform platform);
}
