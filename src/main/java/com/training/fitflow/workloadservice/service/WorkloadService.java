package com.training.fitflow.workloadservice.service;

import com.training.fitflow.workloadservice.storage.WorkloadStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkloadService {
    private final WorkloadStorage storage;


}
