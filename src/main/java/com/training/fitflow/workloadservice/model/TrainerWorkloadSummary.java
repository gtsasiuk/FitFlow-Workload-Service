package com.training.fitflow.workloadservice.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TrainerWorkloadSummary {
    private String username;
    private String firstName;
    private String lastName;
    private Boolean active;
    private Map<Integer, Map<Integer, Long>> yearMonthDuration = new HashMap<>();
}
