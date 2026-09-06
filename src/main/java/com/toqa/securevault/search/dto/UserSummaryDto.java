package com.toqa.securevault.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean enabled;
    private Instant createdAt;
    private boolean currentlyLocked;
    private Instant lockedUntil;
}
