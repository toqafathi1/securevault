package com.toqa.securevault.search.controller;

import com.toqa.securevault.search.service.AdminUserSearchService;
import com.toqa.securevault.search.service.UserSearchCriteria;
import com.toqa.securevault.search.dto.PagedResponse;
import com.toqa.securevault.search.dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserSearchController {

    private final AdminUserSearchService searchService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<UserSummaryDto> search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .usernameContains(username)
                .role(role)
                .onlyLocked(locked)
                .onlyEnabled(enabled)
                .build();

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        return searchService.search(criteria, pageable);
    }
}
