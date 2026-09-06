package com.toqa.securevault.search.controller;

import com.toqa.securevault.search.repository.UserSummaryViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSummaryScheduler {

    private final UserSummaryViewRepository repository;

    @Scheduled(fixedRate = 30_000)
    public void refresh() {
        try {
            repository.refreshConcurrently();
        } catch (Exception e) {
            log.error("Failed to refresh user_summary_mv", e);
        }
    }
}
