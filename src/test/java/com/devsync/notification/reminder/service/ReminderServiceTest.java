package com.devsync.notification.reminder.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.notification.reminder.dto.CreateReminderRequest;
import com.devsync.notification.reminder.dto.ReminderResponse;
import com.devsync.notification.reminder.dto.UpdateReminderRequest;
import com.devsync.notification.reminder.entity.Reminder;
import com.devsync.notification.reminder.entity.ReminderType;
import com.devsync.notification.reminder.repository.ReminderRepository;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private ReminderServiceImpl reminderService;

    private User sampleUser;
    private Team sampleTeam;
    private TeamMember sampleMember;
    private Reminder sampleReminder;
    private UUID userId;
    private UUID teamId;
    private UUID reminderId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        reminderId = UUID.randomUUID();

        sampleUser = User.builder()
                .id(userId)
                .name("Prince")
                .email("prince@devsync.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        sampleTeam = Team.builder()
                .id(teamId)
                .name("DevSync Team")
                .description("Team description")
                .active(true)
                .build();

        sampleMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .user(sampleUser)
                .team(sampleTeam)
                .role(TeamRole.MEMBER)
                .active(true)
                .build();

        sampleReminder = Reminder.builder()
                .id(reminderId)
                .user(sampleUser)
                .team(sampleTeam)
                .type(ReminderType.DAILY_PROGRESS)
                .title("Submit Daily Progress")
                .message("Reminder at 6 PM")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Asia/Kolkata")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createReminder - success with team and active membership")
    void createReminder_Success() {
        CreateReminderRequest request = CreateReminderRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .type(ReminderType.DAILY_PROGRESS)
                .title("Submit Daily Progress")
                .message("Reminder at 6 PM")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Asia/Kolkata")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(userId, teamId)).thenReturn(Optional.of(sampleMember));
        when(reminderRepository.existsByUserIdAndTypeAndTeamIdAndReminderTimeAndActiveTrue(eq(userId), eq(ReminderType.DAILY_PROGRESS), eq(teamId), eq(LocalTime.of(18, 0)))).thenReturn(false);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(sampleReminder);

        ReminderResponse response = reminderService.createReminder(request);

        assertNotNull(response);
        assertEquals(reminderId, response.getId());
        assertEquals("Submit Daily Progress", response.getTitle());
        assertTrue(response.isActive());
        verify(reminderRepository).save(any(Reminder.class));
    }

    @Test
    @DisplayName("createReminder - user not found throws ResourceNotFoundException")
    void createReminder_UserNotFound_ThrowsException() {
        CreateReminderRequest request = CreateReminderRequest.builder()
                .userId(userId)
                .type(ReminderType.DAILY_PROGRESS)
                .title("Title")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Asia/Kolkata")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reminderService.createReminder(request));
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("createReminder - team not found throws ResourceNotFoundException")
    void createReminder_TeamNotFound_ThrowsException() {
        CreateReminderRequest request = CreateReminderRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .type(ReminderType.DAILY_PROGRESS)
                .title("Title")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Asia/Kolkata")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reminderService.createReminder(request));
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("createReminder - user not active team member throws BadRequestException")
    void createReminder_InactiveMembership_ThrowsException() {
        CreateReminderRequest request = CreateReminderRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .type(ReminderType.DAILY_PROGRESS)
                .title("Title")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Asia/Kolkata")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(userId, teamId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> reminderService.createReminder(request));
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("createReminder - invalid timezone throws BadRequestException")
    void createReminder_InvalidTimezone_ThrowsException() {
        CreateReminderRequest request = CreateReminderRequest.builder()
                .userId(userId)
                .type(ReminderType.DAILY_PROGRESS)
                .title("Title")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Invalid/Timezone")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        assertThrows(BadRequestException.class, () -> reminderService.createReminder(request));
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("createReminder - duplicate active reminder throws BadRequestException")
    void createReminder_DuplicateActive_ThrowsException() {
        CreateReminderRequest request = CreateReminderRequest.builder()
                .userId(userId)
                .teamId(teamId)
                .type(ReminderType.DAILY_PROGRESS)
                .title("Title")
                .reminderTime(LocalTime.of(18, 0))
                .timezone("Asia/Kolkata")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(sampleTeam));
        when(teamMemberRepository.findByUserIdAndTeamId(userId, teamId)).thenReturn(Optional.of(sampleMember));
        when(reminderRepository.existsByUserIdAndTypeAndTeamIdAndReminderTimeAndActiveTrue(eq(userId), eq(ReminderType.DAILY_PROGRESS), eq(teamId), eq(LocalTime.of(18, 0)))).thenReturn(true);

        assertThrows(BadRequestException.class, () -> reminderService.createReminder(request));
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("updateReminder - success updating editable fields")
    void updateReminder_Success() {
        UpdateReminderRequest request = UpdateReminderRequest.builder()
                .title("Updated Standup Reminder")
                .message("Updated message")
                .reminderTime(LocalTime.of(19, 0))
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        when(reminderRepository.findById(reminderId)).thenReturn(Optional.of(sampleReminder));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(sampleReminder);

        ReminderResponse response = reminderService.updateReminder(reminderId, request);

        assertNotNull(response);
        assertEquals("Updated Standup Reminder", sampleReminder.getTitle());
        assertEquals(LocalTime.of(19, 0), sampleReminder.getReminderTime());
        verify(reminderRepository).save(sampleReminder);
    }

    @Test
    @DisplayName("deactivateReminder - soft deletes reminder by setting active=false")
    void deactivateReminder_SoftDeletes() {
        when(reminderRepository.findById(reminderId)).thenReturn(Optional.of(sampleReminder));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(sampleReminder);

        reminderService.deactivateReminder(reminderId);

        assertFalse(sampleReminder.isActive());
        verify(reminderRepository).save(sampleReminder);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getUserReminders - paginated list filtering")
    void getUserReminders_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Reminder> page = new PageImpl<>(List.of(sampleReminder));

        when(reminderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ReminderResponse> result = reminderService.getUserReminders(userId, teamId, ReminderType.DAILY_PROGRESS, true, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Submit Daily Progress", result.getContent().get(0).getTitle());
    }
}
