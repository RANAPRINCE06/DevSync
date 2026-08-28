package com.devsync.progress.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.progress.dto.CreateProgressRequest;
import com.devsync.progress.dto.ProgressResponse;
import com.devsync.progress.dto.UpdateProgressRequest;
import com.devsync.progress.entity.DailyProgress;
import com.devsync.progress.entity.ProgressStatus;
import com.devsync.progress.repository.DailyProgressRepository;
import com.devsync.progress.repository.DailyProgressSpecification;
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
import java.util.Optional;
import java.util.UUID;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final DailyProgressRepository dailyProgressRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public ProgressServiceImpl(
            DailyProgressRepository dailyProgressRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository) {
        this.dailyProgressRepository = dailyProgressRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    @Transactional
    public ProgressResponse createProgress(CreateProgressRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.getTeamId()));

        Optional<TeamMember> memberOpt = teamMemberRepository.findByUserIdAndTeamId(user.getId(), team.getId());
        if (memberOpt.isEmpty() || !memberOpt.get().isActive()) {
            throw new BadRequestException("User is not an active member of team '" + team.getName() + "'");
        }

        if (dailyProgressRepository.existsByUserIdAndTeamIdAndProgressDate(user.getId(), team.getId(), request.getProgressDate())) {
            throw new BadRequestException("Progress entry already exists for user in team '" + team.getName() + "' on date: " + request.getProgressDate());
        }

        DailyProgress progress = DailyProgress.builder()
                .user(user)
                .team(team)
                .progressDate(request.getProgressDate())
                .whatStudied(request.getWhatStudied().trim())
                .completed(request.getCompleted().trim())
                .studyMinutes(request.getStudyMinutes())
                .challenges(request.getChallenges() != null ? request.getChallenges().trim() : null)
                .improvementAreas(request.getImprovementAreas() != null ? request.getImprovementAreas().trim() : null)
                .tomorrowPlan(request.getTomorrowPlan() != null ? request.getTomorrowPlan().trim() : null)
                .status(request.getStatus())
                .build();

        DailyProgress saved = dailyProgressRepository.save(progress);
        return ProgressResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressResponse getProgressById(UUID id) {
        DailyProgress progress = dailyProgressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Daily progress entry not found with id: " + id));
        return ProgressResponse.fromEntity(progress);
    }

    @Override
    @Transactional
    public ProgressResponse updateProgress(UUID id, UpdateProgressRequest request) {
        DailyProgress progress = dailyProgressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Daily progress entry not found with id: " + id));

        progress.setWhatStudied(request.getWhatStudied().trim());
        progress.setCompleted(request.getCompleted().trim());
        progress.setStudyMinutes(request.getStudyMinutes());
        progress.setChallenges(request.getChallenges() != null ? request.getChallenges().trim() : null);
        progress.setImprovementAreas(request.getImprovementAreas() != null ? request.getImprovementAreas().trim() : null);
        progress.setTomorrowPlan(request.getTomorrowPlan() != null ? request.getTomorrowPlan().trim() : null);
        progress.setStatus(request.getStatus());

        DailyProgress updated = dailyProgressRepository.save(progress);
        return ProgressResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProgressResponse> getProgressList(
            UUID userId,
            UUID teamId,
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            ProgressStatus status,
            Pageable pageable) {

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate (" + fromDate + ") cannot be after toDate (" + toDate + ")");
        }

        Specification<DailyProgress> spec = DailyProgressSpecification.filter(userId, teamId, date, fromDate, toDate, status);
        return dailyProgressRepository.findAll(spec, pageable)
                .map(ProgressResponse::fromEntity);
    }
}
