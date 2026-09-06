package com.toqa.securevault.search.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable // tells Hibernate: never dirty-check or write back to this — it's a read-only projection
@Table(name = "user_summary_mv")
@Getter
public class UserSummaryView {

    @Id
    private Long id;

    private String username;
    private String email;
    private String role;
    private boolean enabled;
    private Instant createdAt;
    private boolean currentlyLocked;
    private Instant lockedUntil;
}
