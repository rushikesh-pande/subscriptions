package com.subscriptions.db.service;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Database Optimisation Enhancement: Database Metrics Service
 *
 * Tracks cache and query performance metrics for subscriptions.
 * Exposed to Prometheus via /actuator/prometheus.
 *
 * Metrics:
 *  - subscriptions_cache_hits_total       — Redis cache hits
 *  - subscriptions_cache_misses_total     — Redis cache misses (DB queries)
 *  - subscriptions_db_queries_total       — Total DB queries by type
 *  - subscriptions_db_slow_queries_total  — Queries above 500ms
 *  - subscriptions_connection_pool_active — HikariCP active connections
 */
@Service
public class DatabaseMetricsService {

    private final MeterRegistry meterRegistry;
    private final AtomicLong activeConnections = new AtomicLong(0);

    public DatabaseMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        Gauge.builder("subscriptions.connection.pool.active", activeConnections, AtomicLong::get)
             .description("Active HikariCP connections for subscriptions")
             .tag("service", "subscriptions")
             .register(meterRegistry);
    }

    public void recordCacheHit(String cacheName) {
        Counter.builder("subscriptions.cache.hits.total")
               .tag("service", "subscriptions").tag("cache", cacheName)
               .description("Redis cache hits for subscriptions")
               .register(meterRegistry).increment();
    }

    public void recordCacheMiss(String cacheName) {
        Counter.builder("subscriptions.cache.misses.total")
               .tag("service", "subscriptions").tag("cache", cacheName)
               .description("Redis cache misses for subscriptions (DB fallback)")
               .register(meterRegistry).increment();
    }

    public void recordDbQuery(String queryType) {
        Counter.builder("subscriptions.db.queries.total")
               .tag("service", "subscriptions").tag("type", queryType)
               .description("DB queries for subscriptions")
               .register(meterRegistry).increment();
    }

    public void recordSlowQuery(String queryType, long ms) {
        Counter.builder("subscriptions.db.slow.queries.total")
               .tag("service", "subscriptions").tag("type", queryType)
               .description("DB queries exceeding 500ms for subscriptions")
               .register(meterRegistry).increment();
        meterRegistry.summary("subscriptions.db.query.duration",
                "service", "subscriptions", "type", queryType).record(ms);
    }

    public void setActiveConnections(long count) {
        activeConnections.set(count);
    }
}
