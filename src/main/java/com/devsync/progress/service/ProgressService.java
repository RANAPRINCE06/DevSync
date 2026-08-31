package com.devsync.progress.service;

import com.devsync.progress.dto.CreateProgressRequest;
import com.devsync.progress.dto.ProgressResponse;
import com.devsync.progress.dto.UpdateProgressRequest;
import com.devsync.progress.entity.ProgressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface ProgressService {

    ProgressResponse createProgress(CreateProgressRequest request);

    ProgressResponse getProgressById(UUID id);

    ProgressResponse updateProgress(UUID id, UpdateProgressRequest request);

    Page<ProgressResponse> getProgressList(
            UUID userId,
            UUID teamId,
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            ProgressStatus status,
            Pageable pageable);
}
