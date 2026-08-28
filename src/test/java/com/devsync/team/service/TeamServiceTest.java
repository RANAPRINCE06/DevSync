package com.devsync.team.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.team.dto.*;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.entity.TeamRole;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private User creatorUser;
    private Team sampleTeam;
    private UUID userId;
    private UUID teamId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        creatorUser = User.builder()
                .id(userId)
                .name("Prince")
                .email("prince@example.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        sampleTeam = Team.builder()
                .id(teamId)
                .name("DevSync Squad")
                .description("Accountability squad")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createTeam - assigns creator as OWNER transactionally")
    void createTeam_Success_AssignsOwner() {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("DevSync Squad")
                .description("Accountability squad")
                .creatorUserId(userId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(creatorUser));
        when(teamRepository.existsByName("DevSync Squad")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(sampleTeam);

        TeamResponse response = teamService.createTeam(request);

        assertNotNull(response);
        assertEquals("DevSync Squad", response.getName());
        verify(teamRepository).save(any(Team.class));
        verify(teamMemberRepository).save(argThat(member ->
                member.getUser().getId().equals(userId) &&
                member.getRole().equals(TeamRole.OWNER)
        ));
    }

    @Test
    @DisplayName("createTeam - duplicate name throws BadRequestException")
    void createTeam_DuplicateName_ThrowsException() {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("DevSync Squad")
                .creatorUserId(userId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(creatorUser));
        when(teamRepository.existsByName("DevSync Squad")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> teamService.createTeam(request));
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("addMember - success defaults to MEMBER role")
    void addMember_Success() {
        UUID newUserId = UUID.randomUUID();
        User newMemberUser = User.builder()
                .id(newUserId)
                .name("John")
                .email("john@example.com")
                .build();

        TeamMember savedMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(newMemberUser)
                .team(sampleTeam)
                .role(TeamRole.MEMBER)
                .joinedAt(Instant.now())
                .active(true)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newMemberUser));
        when(teamMemberRepository.existsByUserIdAndTeamId(newUserId, teamId)).thenReturn(false);
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(savedMember);

        TeamMemberResponse response = teamService.addMember(teamId, newUserId);

        assertNotNull(response);
        assertEquals(TeamRole.MEMBER, response.getRole());
        assertEquals(newUserId, response.getUserId());
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("addMember - duplicate membership throws BadRequestException")
    void addMember_Duplicate_ThrowsException() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(creatorUser));
        when(teamMemberRepository.existsByUserIdAndTeamId(userId, teamId)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> teamService.addMember(teamId, userId));
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("getTeamById - not found throws ResourceNotFoundException")
    void getTeamById_NotFound_ThrowsException() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamById(teamId));
    }
}
