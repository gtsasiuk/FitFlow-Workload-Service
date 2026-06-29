package com.training.fitflow.workloadservice.controller;

import com.training.fitflow.workloadservice.dto.workload.request.TrainerWorkloadRequest;
import com.training.fitflow.workloadservice.dto.workload.response.TrainerWorkloadResponse;
import com.training.fitflow.workloadservice.exception.InvalidWorkloadQueryException;
import com.training.fitflow.workloadservice.exception.TrainerNotFoundException;
import com.training.fitflow.workloadservice.model.ActionType;
import com.training.fitflow.workloadservice.service.WorkloadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadController Tests")
class WorkloadControllerTest {
    @Mock
    private WorkloadService workloadService;
    @InjectMocks
    private WorkloadController workloadController;

    private TrainerWorkloadRequest sampleRequest() {
        return new TrainerWorkloadRequest(
                "john.trainer", "John", "Doe", true,
                LocalDate.of(2024, 6, 15), 60L, ActionType.ADD
        );
    }

    // ───────────────── updateWorkload ─────────────────

    @Test
    @DisplayName("updateWorkload → valid request → returns 200 and calls service")
    void updateWorkload_validRequest_returns200() {
        TrainerWorkloadRequest request = sampleRequest();

        ResponseEntity<Void> response = workloadController.updateWorkload(request);

        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(workloadService).processWorkload(request);
    }

    @Test
    @DisplayName("updateWorkload → service throws exception → propagates exception")
    void updateWorkload_serviceThrowsException_propagatesException() {
        TrainerWorkloadRequest request = sampleRequest();
        doThrow(new RuntimeException("Unexpected error"))
                .when(workloadService).processWorkload(request);

        assertThrows(RuntimeException.class,
                () -> workloadController.updateWorkload(request)
        );

        verify(workloadService).processWorkload(request);
    }

    @Test
    @DisplayName("updateWorkload → service called exactly once")
    void updateWorkload_serviceCalledExactlyOnce() {
        TrainerWorkloadRequest request = sampleRequest();

        workloadController.updateWorkload(request);

        verify(workloadService, times(1)).processWorkload(request);
        verifyNoMoreInteractions(workloadService);
    }

    // ───────────────── getWorkload ─────────────────

    @Test
    @DisplayName("getWorkload → username only → returns 200 with full summary")
    void getWorkload_usernameOnly_returns200WithFullSummary() {
        TrainerWorkloadResponse expected = new TrainerWorkloadResponse(
                "john.trainer", "John", "Doe", true,
                Map.of(2024, Map.of(6, 60L))
        );
        when(workloadService.getWorkload("john.trainer", null, null)).thenReturn(expected);

        ResponseEntity<TrainerWorkloadResponse> response =
                workloadController.getWorkload("john.trainer", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expected, response.getBody());
        verify(workloadService).getWorkload("john.trainer", null, null);
    }

    @Test
    @DisplayName("getWorkload → with year and month → returns 200 with filtered summary")
    void getWorkload_withYearAndMonth_returns200WithFilteredSummary() {
        TrainerWorkloadResponse expected = new TrainerWorkloadResponse(
                "john.trainer", "John", "Doe", true,
                Map.of(2024, Map.of(6, 60L))
        );
        when(workloadService.getWorkload("john.trainer", 2024, 6)).thenReturn(expected);

        ResponseEntity<TrainerWorkloadResponse> response =
                workloadController.getWorkload("john.trainer", 2024, 6);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expected, response.getBody());
        verify(workloadService).getWorkload("john.trainer", 2024, 6);
    }

    @Test
    @DisplayName("getWorkload → trainer not found → throws TrainerNotFoundException")
    void getWorkload_trainerNotFound_throwsTrainerNotFoundException() {
        doThrow(new TrainerNotFoundException("unknown"))
                .when(workloadService).getWorkload("unknown", null, null);

        assertThrows(TrainerNotFoundException.class,
                () -> workloadController.getWorkload("unknown", null, null)
        );

        verify(workloadService).getWorkload("unknown", null, null);
    }

    @Test
    @DisplayName("getWorkload → month without year → throws InvalidWorkloadQueryException")
    void getWorkload_monthWithoutYear_throwsInvalidWorkloadQueryException() {
        doThrow(new InvalidWorkloadQueryException("Year must be provided when month is specified"))
                .when(workloadService).getWorkload("john.trainer", null, 6);

        assertThrows(InvalidWorkloadQueryException.class,
                () -> workloadController.getWorkload("john.trainer", null, 6)
        );

        verify(workloadService).getWorkload("john.trainer", null, 6);
    }

    @Test
    @DisplayName("getWorkload → service called exactly once")
    void getWorkload_serviceCalledExactlyOnce() {
        TrainerWorkloadResponse expected = new TrainerWorkloadResponse(
                "john.trainer", "John", "Doe", true, Map.of()
        );
        when(workloadService.getWorkload("john.trainer", null, null)).thenReturn(expected);

        workloadController.getWorkload("john.trainer", null, null);

        verify(workloadService, times(1)).getWorkload("john.trainer", null, null);
        verifyNoMoreInteractions(workloadService);
    }
}