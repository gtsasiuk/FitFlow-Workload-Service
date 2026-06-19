package com.training.fitflow.workloadservice.mapper;

import com.training.fitflow.workloadservice.dto.request.TrainerWorkloadRequest;
import com.training.fitflow.workloadservice.dto.response.TrainerWorkloadResponse;
import com.training.fitflow.workloadservice.model.TrainerWorkloadSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkloadMapper {
    @Mapping(source = "active", target = "isActive")
    TrainerWorkloadResponse toResponse(TrainerWorkloadSummary summary);

    @Mapping(source = "isActive", target = "active")
    @Mapping(target = "yearMonthDuration", ignore = true)
    TrainerWorkloadSummary toNewSummary(TrainerWorkloadRequest request);
}
