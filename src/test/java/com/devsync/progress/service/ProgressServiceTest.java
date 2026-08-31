package com.devsync.progress.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.progress.dto.CreateProgressRequest;
import com.devsync.progress.dto.ProgressResponse;
import com.devsync.progress.dto.UpdateProgressRequest;
import com.devsync.progress.entity.DailyProgress;
import com.devsync.progress.entity.ProgressStatus;
import com.devsync.progress.repository.DailyProgressRepository;
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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private DailyProgressRepository dailyProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private ProgressServiceImpl progressService;

    private User sampleUser;
    private Team sampleTeam;
    private TeamMember sampleMember;
    private DailyProgress sampleProgress;
    private UUID userId;
    private UUID teamId;
    private UUID progressId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        progressId = UUID.randomUUID();
        today = LocalDate.now();

        sampleUser = User.builder()
                .id(userId)
                .name("Prince")
                .email("prince@devsync.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        sampleTeam = Team.builder()
                .id(teamId)
                .name("DevSync Squad")
                .description("Accountability squad")
                .active(true)
                .build();

        sampleMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(sampleUser)
                .team(sampleTeam)
                .role(TeamRole.MEMBER)
                .active(true)
                .build();

        sampleProgress = DailyProgress.builder()
                .id(progressId)
                .user(sampleUser)
                .team(sampleTeam)
                .progressDate(today)
                .whatStudied("Spring Data Specifications")
                .completed("Implemented filters")
                .studyMinutes(120)
                .challenges("Complex query testing")
                .improvementAreas("More unit tests")
                .tomorrowPlan("Integrate Step 2B")
                .status(ProgressStatus.COMPLETED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createProgress - success")
    void createProgress_Success() {
        CreateProgressRequest request = CreateProgressRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .progressDate(today)
                .whatStudied("Spring Data Specifications")
                .completed("Implemented filters")
                .studyMinutes(120)
                .status(ProgressStatus.COMPLETED)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(userId, teamId)).thenReturn(Optional.of(sampleMember));
        when(dailyProgressRepository.existsByUserIdAndTeamIdAndProgressDate(userId, teamId, today)).thenReturn(false);
        when(dailyProgressRepository.save(any(DailyProgress.class))).thenReturn(sampleProgress);

        ProgressResponse response = progressService.createProgress(request);

        assertNotNull(response);
        assertEquals(progressId, response.getId());
        assertEquals("Prince", response.getUserName());
        assertEquals("DevSync Squad", response.getTeamName());
        assertEquals(ProgressStatus.COMPLETED, response.getStatus());
        verify(dailyProgressRepository).save(any(DailyProgress.class));
    }

    @Test
    @DisplayName("createProgress - user not found throws ResourceNotFoundException")
    void createProgress_UserNotFound_ThrowsException() {
        CreateProgressRequest request = CreateProgressRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .progressDate(today)
                .whatStudied("Study")
                .completed("Task")
                .studyMinutes(60)
                .status(ProgressStatus.IN_PROGRESS)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> progressService.createProgress(request));
        verify(dailyProgressRepository, never()).save(any(DailyProgress.class));
    }

    @Test
    @DisplayName("createProgress - team not found throws ResourceNotFoundException")
    void createProgress_TeamNotFound_ThrowsException() {
        CreateProgressRequest request = CreateProgressRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .progressDate(today)
                .whatStudied("Study")
                .completed("Task")
                .studyMinutes(60)
                .status(ProgressStatus.IN_PROGRESS)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> progressService.createProgress(request));
        verify(dailyProgressRepository, never()).save(any(DailyProgress.class));
    }

    @Test
    @DisplayName("createProgress - user not active team member throws BadRequestException")
    void createProgress_UserNotMember_ThrowsException() {
        CreateProgressRequest request = CreateProgressRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .progressDate(today)
                .whatStudied("Study")
                .completed("Task")
                .studyMinutes(60)
                .status(ProgressStatus.IN_PROGRESS)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(userId, teamId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> progressService.createProgress(request));
        verify(dailyProgressRepository, never()).save(any(DailyProgress.class));
    }

    @Test
    @DisplayName("createProgress - duplicate daily progress throws BadRequestException")
    void createProgress_Duplicate_ThrowsException() {
        CreateProgressRequest request = CreateProgressRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .progressDate(today)
                .whatStudied("Study")
                .completed("Task")
                .studyMinutes(60)
                .status(ProgressStatus.IN_PROGRESS)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(userId, teamId)).thenReturn(Optional.of(sampleMember));
        when(dailyProgressRepository.existsByUserIdAndTeamIdAndProgressDate(userId, teamId, today)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> progressService.createProgress(request));
        verify(dailyProgressRepository, never()).save(any(DailyProgress.class));
    }

    @Test
    @DisplayName("getProgressById - success")
    void getProgressById_Success() {
        when(dailyProgressRepository.findById(progressId)).thenReturn(Optional.of(sampleProgress));

        ProgressResponse response = progressService.getProgressById(progressId);

        assertNotNull(response);
        assertEquals(progressId, response.getId());
        assertEquals("Prince", response.getUserName());
    }

    @Test
    @DisplayName("getProgressById - not found throws ResourceNotFoundException")
    void getProgressById_NotFound_ThrowsException() {
        when(dailyProgressRepository.findById(progressId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> progressService.getProgressById(progressId));
    }

    @Test
    @DisplayName("updateProgress - success preserving ownership and date")
    void updateProgress_Success() {
        UpdateProgressRequest request = UpdateProgressRequest.builder()
                .whatStudied("Updated Study Content")
                .completed("Updated Tasks")
                .studyMinutes(180)
                .status(ProgressStatus.COMPLETED)
                .build();

        when(dailyProgressRepository.findById(progressId)).thenReturn(Optional.of(sampleProgress));
        when(dailyProgressRepository.save(any(DailyProgress.class))).thenReturn(sampleProgress);

        ProgressResponse response = progressService.updateProgress(progressId, request);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(teamId, response.getTeamId());
        assertEquals(today, response.getProgressDate());
        verify(dailyProgressRepository).save(sampleProgress);
    }

    @Test
    @DisplayName("getProgressList - invalid date range (fromDate > toDate) throws BadRequestException")
    void getProgressList_InvalidDateRange_ThrowsException() {
        LocalDate fromDate = LocalDate.now().plusDays(5);
        LocalDate toDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(BadRequestException.class, () -> progressService.getProgressList(userId, teamId, null, fromDate, toDate, null, pageable));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getProgressList - paginated list success")
    void getProgressList_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "progressDate"));
        Page<DailyProgress> page = new PageImpl<>(List.of(sampleProgress));

        when(dailyProgressRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProgressResponse> result = progressService.getProgressList(userId, teamId, today, null, null, ProgressStatus.COMPLETED, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Prince", result.getContent().get(0).getUserName());
    }
}
