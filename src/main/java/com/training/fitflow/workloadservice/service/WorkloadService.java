package com.training.fitflow.workloadservice.service;

import com.training.fitflow.workloadservice.dto.workload.request.TrainerWorkloadRequest;
import com.training.fitflow.workloadservice.dto.workload.response.TrainerWorkloadResponse;
import com.training.fitflow.workloadservice.exception.InvalidWorkloadQueryException;
import com.training.fitflow.workloadservice.exception.TrainerNotFoundException;
import com.training.fitflow.workloadservice.mapper.WorkloadMapper;
import com.training.fitflow.workloadservice.model.ActionType;
import com.training.fitflow.workloadservice.model.TrainerWorkloadSummary;
import com.training.fitflow.workloadservice.storage.WorkloadStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {
    private final WorkloadStorage storage;
    private final WorkloadMapper mapper;

    public void processWorkload(TrainerWorkloadRequest request) {
        String username = request.trainerUsername();
        log.info("Processing workload change: trainer={}, action={}, date={}, duration={}",
                username, request.actionType(), request.trainingDate(), request.trainingDuration());

        TrainerWorkloadSummary summary = storage.getStorage()
                .computeIfAbsent(username, k -> {
                    log.debug("Creating new workload summary for trainer '{}'", username);
                    return mapper.toNewSummary(request);
                });

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
                long updated = base + delta;
                if (updated < 0) {
                    log.warn("Duration underflow for trainer '{}' {}/{}: clamping to 0", username, year, month);
                }
                return Math.max(0L, updated);
            });

            log.info("Workload updated: trainer={}, year={}, month={}, totalDuration={}",
                    username, year, month, months.get(month));
        }
    }

    public TrainerWorkloadResponse getWorkload(String username, Integer year, Integer month) {
        log.info("Fetching workload: trainer={}, year={}, month={}", username, year, month);

        TrainerWorkloadSummary summary = storage.getStorage().get(username);
        if (summary == null) {
            log.warn("Trainer '{}' not found in workload storage", username);
            throw new TrainerNotFoundException(username);
        }

        synchronized (summary) {
            return mapper.toResponse(filterByPeriod(summary, year, month));
        }
    }

    private TrainerWorkloadSummary filterByPeriod(TrainerWorkloadSummary summary, Integer year, Integer month) {
        if (month != null && year == null) {
            throw new InvalidWorkloadQueryException("Year must be provided when month is specified");
        }

        TrainerWorkloadSummary copy = new TrainerWorkloadSummary();
        copy.setUsername(summary.getUsername());
        copy.setFirstName(summary.getFirstName());
        copy.setLastName(summary.getLastName());
        copy.setActive(summary.getActive());

        if (year == null) {
            copy.setYearMonthDuration(deepCopy(summary.getYearMonthDuration()));
            return copy;
        }

        Map<Integer, Long> sourceMonths = summary.getYearMonthDuration().get(year);
        if (sourceMonths == null) {
            return copy;
        }

        Map<Integer, Long> resultMonths = new HashMap<>();
        if (month != null) {
            Long duration = sourceMonths.get(month);
            if (duration != null) {
                resultMonths.put(month, duration);
            }
        } else {
            resultMonths.putAll(sourceMonths);
        }

        if (!resultMonths.isEmpty()) {
            copy.getYearMonthDuration().put(year, resultMonths);
        }
        return copy;
    }

    private Map<Integer, Map<Integer, Long>> deepCopy(Map<Integer, Map<Integer, Long>> source) {
        Map<Integer, Map<Integer, Long>> copy = new HashMap<>();
        source.forEach((y, months) -> copy.put(y, new HashMap<>(months)));
        return copy;
    }
}