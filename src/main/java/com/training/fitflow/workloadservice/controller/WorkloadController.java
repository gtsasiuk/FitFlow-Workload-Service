package com.training.fitflow.workloadservice.controller;

import com.training.fitflow.workloadservice.dto.request.TrainerWorkloadRequest;
import com.training.fitflow.workloadservice.dto.response.TrainerWorkloadResponse;
import com.training.fitflow.workloadservice.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workload")
@RequiredArgsConstructor
public class WorkloadController {
    private final WorkloadService workloadService;

    @PostMapping
    public ResponseEntity<Void> updateWorkload(@RequestBody @Valid TrainerWorkloadRequest request) {
        workloadService.processWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getWorkload(@PathVariable("username") String username) {
        return ResponseEntity.ok(workloadService.getWorkload(username));
    }
}
