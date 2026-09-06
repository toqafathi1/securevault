package com.toqa.securevault.auth.service;

import com.toqa.securevault.auth.dto.AuthResponse;
import com.toqa.securevault.auth.dto.LoginRequest;
import com.toqa.securevault.auth.dto.RefreshRequest;
import com.toqa.securevault.auth.dto.RegisterRequest;
import com.toqa.securevault.config.JwtProperties;
import com.toqa.securevault.lockout.AccountLockoutService;
import com.toqa.securevault.security.JwtService;
import com.toqa.securevault.token.RefreshTokenService;
import com.toqa.securevault.token.entity.RefreshToken;
import com.toqa.securevault.user.entity.User;
import com.toqa.securevault.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AccountLockoutService lockoutService;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        lockoutService.assertNotLocked(request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException failed) {
            lockoutService.recordFailureWithRetry(request.getUsername());
            throw failed;
        }

        lockoutService.recordSuccess(request.getUsername());

        UserDetails user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("User vanished after successful auth"));

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.issue(user.getUsername());

        return new AuthResponse(accessToken, refreshToken.getToken(), jwtProperties.accessTokenTtlSeconds());
    }

    @Override
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken rotated = refreshTokenService.rotate(request.getRefreshToken());
        UserDetails user = userRepository.findByUsername(rotated.getUsername())
                .orElseThrow(() -> new IllegalStateException("User for refresh token no longer exists"));

        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(accessToken, rotated.getToken(), jwtProperties.accessTokenTtlSeconds());
    }

    @Override
    public void logout(String username) {
        refreshTokenService.revokeAll(username);
    }
}
