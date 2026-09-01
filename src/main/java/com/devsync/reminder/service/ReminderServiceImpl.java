package com.devsync.reminder.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.reminder.dto.CreateReminderRequest;
import com.devsync.reminder.dto.ReminderResponse;
import com.devsync.reminder.dto.UpdateReminderRequest;
import com.devsync.reminder.entity.Reminder;
import com.devsync.reminder.entity.ReminderStatus;
import com.devsync.reminder.entity.ReminderType;
import com.devsync.reminder.repository.ReminderRepository;
import com.devsync.reminder.repository.ReminderSpecification;
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

import java.time.LocalDate;
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

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.getTeamId()));

        Optional<TeamMember> memberOpt = teamMemberRepository.findByUserIdAndTeamId(user.getId(), team.getId());
        if (memberOpt.isEmpty() || !memberOpt.get().isActive()) {
            throw new BadRequestException("User is not an active member of team '" + team.getName() + "'");
        }

        try {
            ZoneId.of(request.getTimezone());
        } catch (Exception e) {
            throw new BadRequestException("Invalid IANA timezone: " + request.getTimezone());
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        Reminder reminder = Reminder.builder()
                .user(user)
                .team(team)
                .type(request.getType())
                .status(ReminderStatus.ACTIVE)
                .title(request.getTitle().trim())
                .message(request.getMessage() != null ? request.getMessage().trim() : null)
                .reminderTime(request.getReminderTime())
                .timezone(request.getTimezone().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
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

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        reminder.setTitle(request.getTitle().trim());
        reminder.setMessage(request.getMessage() != null ? request.getMessage().trim() : null);
        reminder.setReminderTime(request.getReminderTime());
        reminder.setTimezone(request.getTimezone().trim());
        reminder.setStartDate(request.getStartDate());
        reminder.setEndDate(request.getEndDate());
        reminder.setStatus(request.getStatus());
        if (request.getStatus() == ReminderStatus.CANCELLED) {
            reminder.setActive(false);
        } else if (request.getStatus() == ReminderStatus.ACTIVE) {
            reminder.setActive(true);
        }

        Reminder saved = reminderRepository.save(reminder);
        return ReminderResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deactivateReminder(UUID id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
        reminder.setActive(false);
        reminder.setStatus(ReminderStatus.CANCELLED);
        reminderRepository.save(reminder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReminderResponse> getReminders(UUID userId, UUID teamId, ReminderType type, ReminderStatus status, Boolean active, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<Reminder> spec = ReminderSpecification.filter(userId, teamId, type, status, active, startDate, endDate);
        return reminderRepository.findAll(spec, pageable).map(ReminderResponse::fromEntity);
    }
}
