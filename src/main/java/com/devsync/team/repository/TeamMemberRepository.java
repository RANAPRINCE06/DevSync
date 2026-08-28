package com.devsync.team.repository;

import com.devsync.team.entity.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    Page<TeamMember> findByTeamId(UUID teamId, Pageable pageable);

    Page<TeamMember> findByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndTeamId(UUID userId, UUID teamId);

    Optional<TeamMember> findByUserIdAndTeamId(UUID userId, UUID teamId);
}
