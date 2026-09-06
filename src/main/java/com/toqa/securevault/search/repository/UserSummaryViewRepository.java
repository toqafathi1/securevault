package com.toqa.securevault.search.repository;

import com.toqa.securevault.search.entity.UserSummaryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserSummaryViewRepository
        extends JpaRepository<UserSummaryView, Long>, JpaSpecificationExecutor<UserSummaryView> {

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY user_summary_mv", nativeQuery = true)
    void refreshConcurrently();
}
