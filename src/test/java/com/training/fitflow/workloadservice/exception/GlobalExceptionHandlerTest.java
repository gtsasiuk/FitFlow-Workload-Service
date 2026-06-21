package com.training.fitflow.workloadservice.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.training.fitflow.workloadservice.dto.exception.response.ErrorResponse;
import com.training.fitflow.workloadservice.model.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleValidation -> returns bad request with validation message")
    void handleValidation_returnsBadRequest() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);

        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(
                        new FieldError(
                                "request",
                                "trainerUsername",
                                "Trainer username is required"
                        ),
                        new FieldError(
                                "request",
                                "trainingDate",
                                "Training date is required"
                        )
                )
        );

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "trainerUsername: Trainer username is required, " +
                        "trainingDate: Training date is required",
                response.getBody().message()
        );
    }

    @Test
    @DisplayName("handleInvalidQuery -> returns bad request")
    void handleInvalidQuery_returnsBadRequest() {
        InvalidWorkloadQueryException exception =
                new InvalidWorkloadQueryException(
                        "Year must be provided when month is specified"
                );
        ResponseEntity<ErrorResponse> response = handler.handleInvalidQuery(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Year must be provided when month is specified", response.getBody().message());
    }

    @Test
    @DisplayName("handleNotReadable -> malformed request body")
    void handleNotReadable_defaultMessage() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed request");
        ResponseEntity<ErrorResponse> response = handler.handleNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed request body", response.getBody().message());
    }

    @Test
    @DisplayName("handleNotReadable -> invalid enum value")
    void handleNotReadable_invalidEnumValue() {
        InvalidFormatException invalidFormatException =
                new InvalidFormatException(
                        null,
                        "Invalid enum",
                        "WRONG_VALUE",
                        ActionType.class
                );

        invalidFormatException.prependPath(
                new JsonMappingException.Reference(
                        Object.class,
                        "actionType"
                )
        );

        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid body", invalidFormatException);

        ResponseEntity<ErrorResponse> response = handler.handleNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals("Invalid value for field 'actionType'. Accepted values: ADD, DELETE", response.getBody().message());
    }

    @Test
    @DisplayName("handleTrainerNotFound -> returns not found")
    void handleTrainerNotFound_returnsNotFound() {
        TrainerNotFoundException exception = new TrainerNotFoundException("john.trainer");
        ResponseEntity<ErrorResponse> response = handler.handleException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Trainer not found: john.trainer", response.getBody().message());
    }

    @Test
    @DisplayName("handleException -> returns internal server error")
    void handleException_returnsInternalServerError() {
        Exception exception = new RuntimeException("Unexpected");
        ResponseEntity<ErrorResponse> response = handler.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().message());
    }
}