-- Database Optimisation Enhancement: Cache Audit Table
-- Service: subscriptions
-- Version: V2 — cache performance audit

-- Optional: track cache effectiveness
-- CREATE TABLE IF NOT EXISTS cache_audit (
--     id          BIGINT AUTO_INCREMENT PRIMARY KEY,
--     cache_name  VARCHAR(100)  NOT NULL,
--     operation   VARCHAR(20)   NOT NULL,  -- HIT / MISS / EVICT / PUT
--     key_hash    VARCHAR(64)   NOT NULL,
--     duration_ms BIGINT,
--     created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

-- CREATE INDEX IF NOT EXISTS idx_cache_audit_name ON cache_audit (cache_name, created_at DESC);

SELECT 2 AS flyway_v2_check;
