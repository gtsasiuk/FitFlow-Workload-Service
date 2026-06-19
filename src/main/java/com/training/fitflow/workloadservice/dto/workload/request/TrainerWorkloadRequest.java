package com.training.fitflow.workloadservice.dto.workload.request;

import com.training.fitflow.workloadservice.model.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
        @NotBlank String trainerUsername,
        @NotBlank String trainerFirstName,
        @NotBlank String trainerLastName,
        @NotNull Boolean isActive,
        @NotNull LocalDate trainingDate,
        @NotNull Long trainingDuration,
        @NotNull ActionType actionType
) {
}
