package com.devsync.notification.reminder.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.notification.reminder.dto.CreateReminderRequest;
import com.devsync.notification.reminder.dto.ReminderResponse;
import com.devsync.notification.reminder.dto.UpdateReminderRequest;
import com.devsync.notification.reminder.entity.Reminder;
import com.devsync.notification.reminder.entity.ReminderType;
import com.devsync.notification.reminder.repository.ReminderRepository;
import com.devsync.notification.reminder.repository.ReminderSpecification;
import com.devsync.team.entity.Team;
import com.devsync.team.entity.TeamMember;
import com.devsync.team.repository.TeamMemberRepository;
import com.devsync.team.repository.TeamRepository;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public ReminderServiceImpl(
            ReminderRepository reminderRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    @Transactional
    public ReminderResponse createReminder(CreateReminderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.getTeamId()));

            Optional<TeamMember> memberOpt = teamMemberRepository.findByUserIdAndTeamId(user.getId(), team.getId());
            if (memberOpt.isEmpty() || !memberOpt.get().isActive()) {
                throw new BadRequestException("User is not an active member of team '" + team.getName() + "'");
            }
        }

        try {
            ZoneId.of(request.getTimezone());
        } catch (Exception e) {
            throw new BadRequestException("Invalid IANA timezone: " + request.getTimezone());
        }

        boolean duplicateExists;
        if (request.getTeamId() != null) {
            duplicateExists = reminderRepository.existsByUserIdAndTypeAndTeamIdAndReminderTimeAndActiveTrue(
                    user.getId(), request.getType(), request.getTeamId(), request.getReminderTime());
        } else {
            duplicateExists = reminderRepository.existsByUserIdAndTypeAndTeamIsNullAndReminderTimeAndActiveTrue(
                    user.getId(), request.getType(), request.getReminderTime());
        }

        if (duplicateExists) {
            throw new BadRequestException("Active reminder already exists for user with type " + request.getType() + " at " + request.getReminderTime());
        }

        Reminder reminder = Reminder.builder()
                .user(user)
                .team(team)
                .type(request.getType())
                .title(request.getTitle().trim())
                .message(request.getMessage() != null ? request.getMessage().trim() : null)
                .reminderTime(request.getReminderTime())
                .timezone(request.getTimezone().trim())
                .active(true)
                .build();

        Reminder saved = reminderRepository.save(reminder);
        return ReminderResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReminderResponse getReminderById(UUID id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
        return ReminderResponse.fromEntity(reminder);
    }

    @Override
    @Transactional
    public ReminderResponse updateReminder(UUID id, UpdateReminderRequest request) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));

        try {
            ZoneId.of(request.getTimezone());
        } catch (Exception e) {
            throw new BadRequestException("Invalid IANA timezone: " + request.getTimezone());
        }

        reminder.setTitle(request.getTitle().trim());
        reminder.setMessage(request.getMessage() != null ? request.getMessage().trim() : null);
        reminder.setReminderTime(request.getReminderTime());
        reminder.setTimezone(request.getTimezone().trim());
        reminder.setActive(request.getActive());

        Reminder saved = reminderRepository.save(reminder);
        return ReminderResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deactivateReminder(UUID id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
        reminder.setActive(false);
        reminderRepository.save(reminder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReminderResponse> getUserReminders(UUID userId, UUID teamId, ReminderType type, Boolean active, Pageable pageable) {
        Specification<Reminder> spec = ReminderSpecification.filter(userId, teamId, type, active);
        return reminderRepository.findAll(spec, pageable).map(ReminderResponse::fromEntity);
    }
}
