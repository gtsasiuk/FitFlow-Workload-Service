package com.training.fitflow.workloadservice.service;

import com.training.fitflow.workloadservice.dto.request.TrainerWorkloadRequest;
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

    private TrainerWorkloadSummary createNew(TrainerWorkloadRequest request) {
        TrainerWorkloadSummary summary = new TrainerWorkloadSummary();
        summary.setUsername(request.trainerUsername());
        summary.setFirstName(request.trainerFirstName());
        summary.setLastName(request.trainerLastName());
        summary.setActive(request.isActive());
        return summary;
    }

    public void processWorkloadChange(TrainerWorkloadRequest request) {
        String username = request.trainerUsername();
        TrainerWorkloadSummary summary = storage.getStorage()
                .computeIfAbsent(username, k -> createNew(request));

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


}
