package com.devsync.team.repository;

import com.devsync.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByName(String name);

    boolean existsByName(String name);

    @Query(value = "SELECT t FROM Team t JOIN TeamMember tm ON tm.team = t WHERE tm.user.id = :userId AND tm.active = true AND t.active = true",
           countQuery = "SELECT count(t) FROM Team t JOIN TeamMember tm ON tm.team = t WHERE tm.user.id = :userId AND tm.active = true AND t.active = true")
    Page<Team> findActiveTeamsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
