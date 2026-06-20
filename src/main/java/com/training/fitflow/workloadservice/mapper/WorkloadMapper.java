package com.training.fitflow.workloadservice.mapper;

import com.training.fitflow.workloadservice.dto.workload.request.TrainerWorkloadRequest;
import com.training.fitflow.workloadservice.dto.workload.response.TrainerWorkloadResponse;
import com.training.fitflow.workloadservice.model.TrainerWorkloadSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkloadMapper {
    @Mapping(source = "active", target = "isActive")
    TrainerWorkloadResponse toResponse(TrainerWorkloadSummary summary);

    @Mapping(source = "trainerUsername", target = "username")
    @Mapping(source = "trainerFirstName", target = "firstName")
    @Mapping(source = "trainerLastName", target = "lastName")
    @Mapping(source = "isActive", target = "active")
    @Mapping(target = "yearMonthDuration", ignore = true)
    TrainerWorkloadSummary toNewSummary(TrainerWorkloadRequest request);
}
