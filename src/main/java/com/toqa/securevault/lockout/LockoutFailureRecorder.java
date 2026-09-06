package com.toqa.securevault.lockout;

import com.toqa.securevault.lockout.entity.LoginLockout;
import com.toqa.securevault.lockout.repository.LoginLockoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
class LockoutFailureRecorder {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration[] LOCKOUT_DURATIONS = {
            Duration.ofMinutes(10), Duration.ofMinutes(20), Duration.ofMinutes(40), Duration.ofMinutes(80)
    };
    private static final Duration VIOLATION_LEVEL_TTL = Duration.ofHours(24);

    private final LoginLockoutRepository lockoutRepository;

    @Transactional
    public void recordFailure(String identifier) {
        Instant now = Instant.now();
        LoginLockout lockout = lockoutRepository.findByIdentifier(identifier)
                .orElseGet(() -> newLockout(identifier));

        if (lockout.getWindowStartedAt() == null
                || Duration.between(lockout.getWindowStartedAt(), now).compareTo(WINDOW) > 0) {
            lockout.setWindowStartedAt(now);
            lockout.setFailedAttempts(0);
        }
        lockout.setFailedAttempts(lockout.getFailedAttempts() + 1);

        if (lockout.getFailedAttempts() >= MAX_ATTEMPTS) {
            applyEscalatingLockout(lockout, now);
        }
        lockoutRepository.save(lockout);
    }

    private void applyEscalatingLockout(LoginLockout lockout, Instant now) {
        if (lockout.getViolationLevelExpiresAt() == null || now.isAfter(lockout.getViolationLevelExpiresAt())) {
            lockout.setViolationLevel(0);
        }
        int newLevel = Math.min(lockout.getViolationLevel() + 1, LOCKOUT_DURATIONS.length);
        Duration duration = LOCKOUT_DURATIONS[newLevel - 1];

        lockout.setViolationLevel(newLevel);
        lockout.setViolationLevelExpiresAt(now.plus(VIOLATION_LEVEL_TTL));
        lockout.setLockedUntil(now.plus(duration));
        lockout.setFailedAttempts(0);
        lockout.setWindowStartedAt(null);

        log.warn("Account {} locked for {} minutes (violation level {})",
                lockout.getIdentifier(), duration.toMinutes(), newLevel);
    }

    private LoginLockout newLockout(String identifier) {
        LoginLockout lockout = new LoginLockout();
        lockout.setIdentifier(identifier);
        return lockout;
    }
}
