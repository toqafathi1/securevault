package com.toqa.securevault.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;

    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message);
    }
}
