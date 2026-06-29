package com.training.fitflow.workloadservice.service;

import com.training.fitflow.workloadservice.dto.workload.request.TrainerWorkloadRequest;
import com.training.fitflow.workloadservice.dto.workload.response.TrainerWorkloadResponse;
import com.training.fitflow.workloadservice.exception.InvalidWorkloadQueryException;
import com.training.fitflow.workloadservice.exception.TrainerNotFoundException;
import com.training.fitflow.workloadservice.mapper.WorkloadMapper;
import com.training.fitflow.workloadservice.model.ActionType;
import com.training.fitflow.workloadservice.model.TrainerWorkloadSummary;
import com.training.fitflow.workloadservice.storage.WorkloadStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadService Tests")
class WorkloadServiceTest {
    @Mock
    private WorkloadStorage storage;
    @Mock
    private WorkloadMapper mapper;
    @InjectMocks
    private WorkloadService workloadService;

    private Map<String, TrainerWorkloadSummary> storageMap;
    private TrainerWorkloadRequest addRequest;
    private TrainerWorkloadSummary summary;

    @BeforeEach
    void setUp() {
        storageMap = new HashMap<>();
        when(storage.getStorage()).thenReturn(storageMap);

        addRequest = new TrainerWorkloadRequest(
                "john.trainer",
                "John",
                "Doe",
                true,
                LocalDate.of(2025, 5, 15),
                120L,
                ActionType.ADD
        );

        summary = new TrainerWorkloadSummary();
        summary.setUsername("john.trainer");
        summary.setFirstName("John");
        summary.setLastName("Doe");
        summary.setActive(true);
    }

    @Test
    @DisplayName("processWorkload -> new trainer -> summary created")
    void processWorkload_newTrainer_created() {
        when(mapper.toNewSummary(addRequest)).thenReturn(summary);
        workloadService.processWorkload(addRequest);

        assertTrue(storageMap.containsKey("john.trainer"));
        assertEquals(120L, storageMap.get("john.trainer").getYearMonthDuration().get(2025).get(5));

        verify(mapper).toNewSummary(addRequest);
    }

    @Test
    @DisplayName("processWorkload -> existing trainer -> duration added")
    void processWorkload_existingTrainer_addDuration() {
        summary.getYearMonthDuration().computeIfAbsent(2025, y -> new HashMap<>()).put(5, 100L);
        storageMap.put("john.trainer", summary);

        workloadService.processWorkload(addRequest);

        assertEquals(220L, summary.getYearMonthDuration().get(2025).get(5));
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("processWorkload -> delete workload")
    void processWorkload_deleteDuration() {
        summary.getYearMonthDuration().computeIfAbsent(2025, y -> new HashMap<>()).put(5, 200L);
        storageMap.put("john.trainer", summary);

        TrainerWorkloadRequest deleteRequest =
                new TrainerWorkloadRequest(
                        "john.trainer",
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2025, 5, 15),
                        50L,
                        ActionType.DELETE
                );

        workloadService.processWorkload(deleteRequest);

        assertEquals(150L, summary.getYearMonthDuration().get(2025).get(5));
    }

    @Test
    @DisplayName("processWorkload -> delete more than existing -> clamped to zero")
    void processWorkload_underflow_clampedToZero() {
        summary.getYearMonthDuration().computeIfAbsent(2025, y -> new HashMap<>()).put(5, 100L);
        storageMap.put("john.trainer", summary);

        TrainerWorkloadRequest deleteRequest =
                new TrainerWorkloadRequest(
                        "john.trainer",
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2025, 5, 15),
                        500L,
                        ActionType.DELETE
                );

        workloadService.processWorkload(deleteRequest);

        assertEquals(0L, summary.getYearMonthDuration().get(2025).get(5));
    }

    @Test
    @DisplayName("processWorkload -> active status updated")
    void processWorkload_updatesActiveStatus() {
        summary.setActive(true);
        storageMap.put("john.trainer", summary);

        TrainerWorkloadRequest request =
                new TrainerWorkloadRequest(
                        "john.trainer",
                        "John",
                        "Doe",
                        false,
                        LocalDate.of(2025, 5, 15),
                        10L,
                        ActionType.ADD
                );

        workloadService.processWorkload(request);
        assertFalse(summary.getActive());
    }

    @Test
    @DisplayName("getWorkload -> trainer not found")
    void getWorkload_trainerNotFound() {
        assertThrows(
                TrainerNotFoundException.class,
                () -> workloadService.getWorkload("unknown", null, null)
        );
    }

    @Test
    @DisplayName("getWorkload -> all workload")
    void getWorkload_allData() {
        summary.getYearMonthDuration().computeIfAbsent(2025, y -> new HashMap<>()).put(5, 120L);
        storageMap.put("john.trainer", summary);

        TrainerWorkloadResponse response =
                new TrainerWorkloadResponse(
                        "john.trainer",
                        "John",
                        "Doe",
                        true,
                        summary.getYearMonthDuration()
                );

        when(mapper.toResponse(any())).thenReturn(response);

        TrainerWorkloadResponse result = workloadService.getWorkload("john.trainer", null, null);

        assertEquals("john.trainer", result.username());
        verify(mapper).toResponse(any());
    }

    @Test
    @DisplayName("getWorkload -> month without year -> exception")
    void getWorkload_monthWithoutYear_throwsException() {
        storageMap.put("john.trainer", summary);

        assertThrows(
                InvalidWorkloadQueryException.class,
                () -> workloadService.getWorkload("john.trainer", null, 5)
        );
    }

    @Test
    @DisplayName("getWorkload -> year filter")
    void getWorkload_filterByYear() {
        summary.getYearMonthDuration().computeIfAbsent(2025, y -> new HashMap<>()).put(5, 120L);
        summary.getYearMonthDuration().computeIfAbsent(2024, y -> new HashMap<>()).put(10, 300L);

        storageMap.put("john.trainer", summary);

        when(mapper.toResponse(any())).thenAnswer(invocation -> {
            TrainerWorkloadSummary arg = invocation.getArgument(0);

            return new TrainerWorkloadResponse(
                    arg.getUsername(),
                    arg.getFirstName(),
                    arg.getLastName(),
                    arg.getActive(),
                    arg.getYearMonthDuration()
            );
        });

        TrainerWorkloadResponse result = workloadService.getWorkload("john.trainer", 2025, null);

        assertEquals(1, result.yearMonthDuration().size());
        assertTrue(result.yearMonthDuration().containsKey(2025));
    }

    @Test
    @DisplayName("getWorkload -> year and month filter")
    void getWorkload_filterByYearAndMonth() {
        summary.getYearMonthDuration().computeIfAbsent(2025, y -> new HashMap<>()).put(5, 120L);
        summary.getYearMonthDuration().get(2025).put(6, 300L);
        storageMap.put("john.trainer", summary);

        when(mapper.toResponse(any())).thenAnswer(invocation -> {
            TrainerWorkloadSummary arg = invocation.getArgument(0);

            return new TrainerWorkloadResponse(
                    arg.getUsername(),
                    arg.getFirstName(),
                    arg.getLastName(),
                    arg.getActive(),
                    arg.getYearMonthDuration()
            );
        });

        TrainerWorkloadResponse result = workloadService.getWorkload("john.trainer", 2025, 5);
        assertEquals(1, result.yearMonthDuration().get(2025).size());
        assertEquals(120L, result.yearMonthDuration().get(2025).get(5));
    }
}