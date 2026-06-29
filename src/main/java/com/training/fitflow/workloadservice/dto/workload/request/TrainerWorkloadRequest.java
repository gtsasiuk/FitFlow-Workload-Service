package com.training.fitflow.workloadservice.dto.workload.request;

import com.training.fitflow.workloadservice.model.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
        @NotBlank(message = "Trainer username is required")
        String trainerUsername,
        @NotBlank(message = "Trainer first name is required")
        String trainerFirstName,
        @NotBlank(message = "Trainer last name is required")
        String trainerLastName,
        @NotNull(message = "Trainer active status is required")
        Boolean isActive,
        @NotNull(message = "Training date is required")
        LocalDate trainingDate,
        @NotNull(message = "Training duration is required")
        Long trainingDuration,
        @NotNull(message = "Action type is required")
        ActionType actionType
) {
}
