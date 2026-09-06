package com.toqa.securevault;

import com.toqa.securevault.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SecureVault — a focused showcase of backend security concepts:
 * JWT access + refresh tokens, escalating account lockout with optimistic
 * locking, a denormalized read-model refreshed on a schedule, and the
 * Strategy/Decorator/Builder patterns applied to real code paths.
 *
 * See README.md for the "vulnerable vs fixed" walkthroughs.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
public class SecureVaultApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureVaultApplication.class, args);
    }
}
