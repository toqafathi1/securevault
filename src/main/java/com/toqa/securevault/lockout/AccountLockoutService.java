package com.toqa.securevault.lockout;

import com.toqa.securevault.exception.AccountLockedException;
import com.toqa.securevault.lockout.repository.LoginLockoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockoutService {

    private static final int MAX_RETRIES_ON_CONFLICT = 3;

    private final LoginLockoutRepository lockoutRepository;
    private final LockoutFailureRecorder failureRecorder;

    /** Call BEFORE attempting authentication. Throws if currently locked. */
    @Transactional(readOnly = true)
    public void assertNotLocked(String identifier) {
        Instant now = Instant.now();
        lockoutRepository.findByIdentifier(identifier).ifPresent(lockout -> {
            if (lockout.isCurrentlyLocked(now)) {
                long remainingSeconds = Duration.between(now, lockout.getLockedUntil()).getSeconds();
                throw new AccountLockedException(identifier, remainingSeconds);
            }
        });
    }


    @Transactional
    public void recordSuccess(String identifier) {
        lockoutRepository.findByIdentifier(identifier).ifPresent(lockout -> {
            lockout.setFailedAttempts(0);
            lockout.setWindowStartedAt(null);
            lockoutRepository.save(lockout);
        });
    }


    public void recordFailureWithRetry(String identifier) {
        for (int attempt = 1; attempt <= MAX_RETRIES_ON_CONFLICT; attempt++) {
            try {
                failureRecorder.recordFailure(identifier); // goes through the Spring proxy correctly
                return;
            } catch (ObjectOptimisticLockingFailureException conflict) {
                log.debug("Optimistic lock conflict recording failure for {}, retry {}/{}",
                        identifier, attempt, MAX_RETRIES_ON_CONFLICT);
            }
        }
        log.warn("Gave up recording failed login for {} after {} conflicting retries", identifier, MAX_RETRIES_ON_CONFLICT);
    }
}
