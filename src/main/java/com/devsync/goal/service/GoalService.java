package com.devsync.goal.service;

import com.devsync.goal.dto.CreateGoalRequest;
import com.devsync.goal.dto.GoalResponse;
import com.devsync.goal.dto.UpdateGoalRequest;
import com.devsync.goal.entity.GoalPriority;
import com.devsync.goal.entity.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface GoalService {

    GoalResponse createGoal(CreateGoalRequest request);

    GoalResponse getGoalById(UUID id);

    GoalResponse updateGoal(UUID id, UpdateGoalRequest request);

    void deactivateGoal(UUID id);

    Page<GoalResponse> getGoals(
            UUID ownerId,
            UUID teamId,
            GoalStatus status,
            GoalPriority priority,
            Boolean active,
            LocalDate startDate,
            LocalDate targetDate,
            Pageable pageable);
}
