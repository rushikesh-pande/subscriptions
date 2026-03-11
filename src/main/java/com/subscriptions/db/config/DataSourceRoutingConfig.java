package com.subscriptions.db.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Optimisation Enhancement: Read-Replica DataSource Routing
 *
 * Routes database operations:
 *  - @Transactional(readOnly = true)  → READ  replica DataSource
 *  - @Transactional                   → WRITE primary DataSource
 *
 * In production set datasource.primary.url and datasource.replica.url
 * to different JDBC connection strings.
 */
@Configuration
@EnableTransactionManagement
public class DataSourceRoutingConfig {

    // ── Primary (write) DataSource ────────────────────────────────────────────
    @Bean(name = "primaryDataSource")
    @Primary
    public DataSource primaryDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username:sa}") String user,
            @Value("${spring.datasource.password:}") String pass,
            @Value("${spring.datasource.driver-class-name:org.h2.Driver}") String driver) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setDriverClassName(driver);
        cfg.setPoolName("subscriptions-primary-pool");
        cfg.setMaximumPoolSize(20);
        cfg.setMinimumIdle(5);
        cfg.setConnectionTimeout(30_000);
        cfg.setIdleTimeout(600_000);
        cfg.setMaxLifetime(1_800_000);
        cfg.setLeakDetectionThreshold(60_000);
        // Hikari metrics via Micrometer
        cfg.setMetricRegistry(null);   // injected automatically by Spring Boot
        return new HikariDataSource(cfg);
    }

    // ── Replica (read-only) DataSource ────────────────────────────────────────
    @Bean(name = "replicaDataSource")
    public DataSource replicaDataSource(
            @Value("${datasource.replica.url:${spring.datasource.url}}") String url,
            @Value("${datasource.replica.username:${spring.datasource.username:sa}}") String user,
            @Value("${datasource.replica.password:${spring.datasource.password:}}") String pass,
            @Value("${spring.datasource.driver-class-name:org.h2.Driver}") String driver) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setDriverClassName(driver);
        cfg.setPoolName("subscriptions-replica-pool");
        cfg.setMaximumPoolSize(30);   // replicas handle more read load
        cfg.setMinimumIdle(5);
        cfg.setReadOnly(true);
        return new HikariDataSource(cfg);
    }

    // ── Routing DataSource ────────────────────────────────────────────────────
    @Bean
    @Primary
    @DependsOn({"primaryDataSource", "replicaDataSource"})
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primary,
            @Qualifier("replicaDataSource") DataSource replica) {
        Map<Object, Object> targets = new HashMap<>();
        targets.put(DataSourceType.PRIMARY, primary);
        targets.put(DataSourceType.REPLICA, replica);

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return DataSourceContextHolder.get();
            }
        };
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(primary);
        routing.afterPropertiesSet();
        return routing;
    }

    /** DataSource type enum */
    public enum DataSourceType { PRIMARY, REPLICA }

    /** Thread-local holder for current datasource context */
    public static class DataSourceContextHolder {
        private static final ThreadLocal<DataSourceType> CONTEXT =
                new ThreadLocal<>();
        public static void set(DataSourceType type) { CONTEXT.set(type); }
        public static DataSourceType get() {
            return CONTEXT.get() == null ? DataSourceType.PRIMARY : CONTEXT.get();
        }
        public static void clear() { CONTEXT.remove(); }
    }
}
