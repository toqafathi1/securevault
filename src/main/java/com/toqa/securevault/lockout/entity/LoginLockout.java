package com.toqa.securevault.lockout.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "login_lockout")
@Getter
@Setter
@NoArgsConstructor
public class LoginLockout {

    @Id
    @Column(name = "identifier", length = 150)
    private String identifier; // username or email — whatever the login form submits

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "window_started_at")
    private Instant windowStartedAt;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "violation_level", nullable = false)
    private int violationLevel = 0;

    @Column(name = "violation_level_expires_at")
    private Instant violationLevelExpiresAt;

    @Version
    private Long version;

    public boolean isCurrentlyLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }
}
