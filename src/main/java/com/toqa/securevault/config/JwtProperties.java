package com.toqa.securevault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "securevault.jwt")
public record JwtProperties(
        String secret,               // base64-encoded signing key — MUST come from env in real deployments
        long accessTokenTtlSeconds,  // short-lived — see README on why access/refresh are split
        long refreshTokenTtlSeconds  // long-lived, but revocable server-side (unlike the access token)
) {
}
