package com.toqa.securevault.token;

import com.toqa.securevault.config.JwtProperties;
import com.toqa.securevault.token.entity.RefreshToken;
import com.toqa.securevault.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RefreshToken issue(String username) {
        RefreshToken token = new RefreshToken(
                generateOpaqueToken(),
                username,
                Instant.now().plusSeconds(properties.refreshTokenTtlSeconds())
        );
        return repository.save(token);
    }

    @Transactional
    public RefreshToken rotate(String presentedToken) {
        RefreshToken existing = repository.findByToken(presentedToken)
                .orElseThrow(() -> new IllegalArgumentException("Unknown refresh token"));

        if (existing.isRevoked()) {
            log.warn("SECURITY: reuse of a revoked refresh token detected for user {} — revoking entire session family", existing.getUsername());
            repository.revokeAllForUser(existing.getUsername());
            throw new IllegalArgumentException("Refresh token has been revoked — please log in again");
        }
        if (existing.isExpired()) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        RefreshToken next = issue(existing.getUsername());
        existing.setRevoked(true);
        existing.setReplacedByToken(next.getToken());
        repository.save(existing);
        return next;
    }

    @Transactional
    public void revokeAll(String username) {
        repository.revokeAllForUser(username);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
