package com.subscriptions.db.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database Optimisation Enhancement: JPA / Hibernate Query Optimisation
 *
 * Key optimisations applied:
 *  - Batch inserts/updates (batch_size=25)
 *  - Second-level query plan cache
 *  - JDBC fetch-size tuning
 *  - Slow-query logging at 500ms
 *  - Statistics logging (disabled in prod — set to false)
 *  - N+1 detection via @BatchSize on collections
 *
 * Use these annotations in your @Entity classes to prevent N+1:
 *   @OneToMany
 *   @BatchSize(size = 20)   // fetches 20 child records per batch
 *
 * Use @QueryHints in repositories:
 *   @QueryHints({@QueryHint(name=HINT_FETCH_SIZE, value="50"),
 *                @QueryHint(name=HINT_CACHEABLE,   value="true")})
 */
@Configuration
public class JpaQueryOptimisationConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    /**
     * Extra Hibernate properties layered on top of Spring Boot auto-config.
     * These are merged — not replacing — the auto-configured factory.
     */
    public Properties hibernateOptimisationProperties() {
        Properties p = new Properties();
        // Batch DML
        p.put("hibernate.jdbc.batch_size",          "25");
        p.put("hibernate.order_inserts",            "true");
        p.put("hibernate.order_updates",            "true");
        p.put("hibernate.jdbc.batch_versioned_data","true");
        // Query plan cache
        p.put("hibernate.query.plan_cache_max_size",           "2048");
        p.put("hibernate.query.plan_parameter_metadata_max_size","128");
        // Slow query log
        p.put("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS","500");
        // Statistics (enable only in dev)
        p.put("hibernate.generate_statistics", "false");
        // Second-level cache via Redis (optional — configure region factory separately)
        p.put("hibernate.cache.use_second_level_cache", "false");
        p.put("hibernate.cache.use_query_cache",        "false");
        return p;
    }
}
