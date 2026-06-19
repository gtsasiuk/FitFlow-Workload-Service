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

@Service
@RequiredArgsConstructor
public class WorkloadService {
    private final WorkloadStorage storage;
    private final WorkloadMapper mapper;

    public void processWorkload(TrainerWorkloadRequest request) {
        String username = request.trainerUsername();
        TrainerWorkloadSummary summary = storage.getStorage()
                .computeIfAbsent(username, k -> mapper.toNewSummary(request));

        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();

        summary.getYearMonthDuration()
                .computeIfAbsent(year, k -> new HashMap<>());

        if (request.actionType() == ActionType.ADD) {
            summary.getYearMonthDuration().get(year)
                    .merge(month, request.trainingDuration(), Long::sum);
        } else {
            summary.getYearMonthDuration().get(year)
                    .merge(month, request.trainingDuration(), (current, delta) -> Math.max(0, current - delta));
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
