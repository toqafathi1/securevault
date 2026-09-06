package com.toqa.securevault.search.service;

import com.toqa.securevault.search.entity.UserSummaryView;
import org.springframework.data.jpa.domain.Specification;

public final class UserSearchCriteria {

    private final String usernameContains;
    private final String role;
    private final Boolean onlyLocked;
    private final Boolean onlyEnabled;

    private UserSearchCriteria(Builder builder) {
        this.usernameContains = builder.usernameContains;
        this.role = builder.role;
        this.onlyLocked = builder.onlyLocked;
        this.onlyEnabled = builder.onlyEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Specification<UserSummaryView> toSpecification() {
        Specification<UserSummaryView> spec = Specification.where(null);

        if (usernameContains != null && !usernameContains.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("username")), "%" + usernameContains.toLowerCase() + "%"));
        }
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (onlyLocked != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("currentlyLocked"), onlyLocked));
        }
        if (onlyEnabled != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("enabled"), onlyEnabled));
        }
        return spec;
    }

    public static final class Builder {
        private String usernameContains;
        private String role;
        private Boolean onlyLocked;
        private Boolean onlyEnabled;

        public Builder usernameContains(String value) { this.usernameContains = value; return this; }
        public Builder role(String value) { this.role = value; return this; }
        public Builder onlyLocked(Boolean value) { this.onlyLocked = value; return this; }
        public Builder onlyEnabled(Boolean value) { this.onlyEnabled = value; return this; }

        public UserSearchCriteria build() {
            return new UserSearchCriteria(this);
        }
    }
}
