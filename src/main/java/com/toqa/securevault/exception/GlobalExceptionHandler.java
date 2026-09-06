package com.toqa.securevault.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiError> handleLocked(AccountLockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(ApiError.of(403, "ACCOUNT_LOCKED", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        // Deliberately generic message — never reveal whether the USERNAME
        // or the PASSWORD was wrong; that distinction is exactly the kind
        // of detail that helps an attacker enumerate valid usernames.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "INVALID_CREDENTIALS", "Invalid username or password"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "BAD_REQUEST", ex.getMessage()));
    }
}
