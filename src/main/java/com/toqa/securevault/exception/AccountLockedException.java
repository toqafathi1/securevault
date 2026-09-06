package com.toqa.securevault.exception;

import lombok.Getter;

@Getter
public class AccountLockedException extends RuntimeException {
    private final String identifier;
    private final long retryAfterSeconds;

    public AccountLockedException(String identifier, long retryAfterSeconds) {
        super("Account temporarily locked due to too many failed attempts");
        this.identifier = identifier;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
