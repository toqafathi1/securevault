-- The denormalized read model: pre-joins app_user with its current lockout
-- state so the admin search screen never has to join two live tables.
CREATE MATERIALIZED VIEW user_summary_mv AS
SELECT
    u.id                                                        AS id,
    u.username                                                  AS username,
    u.email                                                      AS email,
    u.role                                                       AS role,
    u.enabled                                                    AS enabled,
    u.created_at                                                 AS created_at,
    (l.locked_until IS NOT NULL AND l.locked_until > now())       AS currently_locked,
    l.locked_until                                                AS locked_until
FROM app_user u
LEFT JOIN login_lockout l ON l.identifier = u.username;

-- REQUIRED for REFRESH MATERIALIZED VIEW CONCURRENTLY to work at all —
-- without a unique index, Postgres refuses the CONCURRENTLY option.
CREATE UNIQUE INDEX idx_user_summary_mv_id ON user_summary_mv (id);
