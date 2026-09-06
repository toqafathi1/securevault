package com.toqa.securevault.lockout.repository;

import com.toqa.securevault.lockout.entity.LoginLockout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginLockoutRepository extends JpaRepository<LoginLockout, String> {
    Optional<LoginLockout> findByIdentifier(String identifier);
}
