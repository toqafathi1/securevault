package com.toqa.securevault.auth.service;

import com.toqa.securevault.auth.dto.AuthResponse;
import com.toqa.securevault.auth.dto.LoginRequest;
import com.toqa.securevault.auth.dto.RefreshRequest;
import com.toqa.securevault.auth.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshRequest request);
    void logout(String username);
}
