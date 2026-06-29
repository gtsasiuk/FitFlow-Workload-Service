package com.training.fitflow.workloadservice.exception;

public class TrainerNotFoundException extends RuntimeException {
    public TrainerNotFoundException(String username) {
        super("Trainer not found: " + username);
    }
}
