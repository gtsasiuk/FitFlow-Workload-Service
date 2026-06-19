package com.training.fitflow.workloadservice.dto.response;

import java.util.Map;

public record TrainerWorkloadResponse(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        Map<Integer, Map<Integer, Long>> yearMonthDuration
) {}
