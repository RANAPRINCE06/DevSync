package com.devsync.coding.intelligence.repository;

import com.devsync.coding.intelligence.entity.GitHubRepositoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitHubRepositoryRecordRepository extends JpaRepository<GitHubRepositoryRecord, UUID> {

    List<GitHubRepositoryRecord> findAllByPlatformAccountIdOrderByStarsCountDesc(UUID platformAccountId);

    @Query("SELECT r FROM GitHubRepositoryRecord r JOIN r.platformAccount acc WHERE acc.user.id = :userId ORDER BY r.starsCount DESC, r.recentCommitsCount DESC")
    List<GitHubRepositoryRecord> findAllByUserId(@Param("userId") UUID userId);

    Optional<GitHubRepositoryRecord> findByPlatformAccountIdAndRepoName(UUID platformAccountId, String repoName);
}
