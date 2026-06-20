package com.training.fitflow.workloadservice.service;

import com.training.fitflow.workloadservice.dto.workload.request.TrainerWorkloadRequest;
import com.training.fitflow.workloadservice.dto.workload.response.TrainerWorkloadResponse;
import com.training.fitflow.workloadservice.exception.TrainerNotFoundException;
import com.training.fitflow.workloadservice.mapper.WorkloadMapper;
import com.training.fitflow.workloadservice.model.ActionType;
import com.training.fitflow.workloadservice.model.TrainerWorkloadSummary;
import com.training.fitflow.workloadservice.storage.WorkloadStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkloadService {
    private final WorkloadStorage storage;
    private final WorkloadMapper mapper;

    public void processWorkload(TrainerWorkloadRequest request) {
        String username = request.trainerUsername();
        TrainerWorkloadSummary summary = storage.getStorage()
                .computeIfAbsent(username, k -> mapper.toNewSummary(request));

        synchronized (summary) {
            summary.setActive(request.isActive());

            int year = request.trainingDate().getYear();
            int month = request.trainingDate().getMonthValue();
            long delta = request.actionType() == ActionType.ADD
                    ? request.trainingDuration()
                    : -request.trainingDuration();

            Map<Integer, Long> months = summary.getYearMonthDuration()
                    .computeIfAbsent(year, k -> new HashMap<>());

            months.compute(month, (k, current) -> {
                long base = current == null ? 0L : current;
                return Math.max(0L, base + delta);
            });
        }
    }

    public TrainerWorkloadResponse getWorkload(String username) {
        TrainerWorkloadSummary summary = storage.getStorage().get(username);
        if (summary == null) {
            throw new TrainerNotFoundException(username);
        }
        return mapper.toResponse(summary);
    }
}
