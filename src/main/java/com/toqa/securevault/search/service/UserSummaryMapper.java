package com.toqa.securevault.search.service;

import com.toqa.securevault.search.dto.UserSummaryDto;
import com.toqa.securevault.search.entity.UserSummaryView;
import org.springframework.stereotype.Component;

@Component
public class UserSummaryMapper {

    public UserSummaryDto toDto(UserSummaryView entity) {
        return UserSummaryDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .enabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .currentlyLocked(entity.isCurrentlyLocked())
                .lockedUntil(entity.getLockedUntil())
                .build();
    }
}
