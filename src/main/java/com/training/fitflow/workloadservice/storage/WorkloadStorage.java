package com.training.fitflow.workloadservice.storage;

import com.training.fitflow.workloadservice.model.TrainerWorkloadSummary;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class WorkloadStorage {
    private final Map<String, TrainerWorkloadSummary> storage = new ConcurrentHashMap<>();
}
