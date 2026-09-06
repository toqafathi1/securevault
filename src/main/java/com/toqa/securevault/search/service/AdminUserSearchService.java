package com.toqa.securevault.search.service;

import com.toqa.securevault.search.dto.PagedResponse;
import com.toqa.securevault.search.dto.UserSummaryDto;
import com.toqa.securevault.search.repository.UserSummaryViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserSearchService {

    private final UserSummaryViewRepository repository;
    private final UserSummaryMapper mapper;

    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryDto> search(UserSearchCriteria criteria, Pageable pageable) {
        Page<UserSummaryDto> page = repository.findAll(criteria.toSpecification(), pageable)
                .map(mapper::toDto);
        return PagedResponse.from(page);
    }
}
