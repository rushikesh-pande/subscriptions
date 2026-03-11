package com.subscriptions.db.aspect;

import com.subscriptions.db.config.DataSourceRoutingConfig.DataSourceContextHolder;
import com.subscriptions.db.config.DataSourceRoutingConfig.DataSourceType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * Database Optimisation Enhancement: Read-Replica Routing Aspect
 *
 * Intercepts @Transactional(readOnly=true) annotated methods and
 * routes them to the READ replica DataSource automatically.
 * Write transactions continue to use the PRIMARY DataSource.
 *
 * Must run BEFORE @Transactional (Order = -1).
 */
@Aspect
@Component
@Order(-1)
public class ReadOnlyTransactionAspect {

    @Around("@annotation(transactional)")
    public Object routeDataSource(ProceedingJoinPoint pjp,
                                   Transactional transactional) throws Throwable {
        boolean readOnly = transactional.readOnly();
        DataSourceType target = readOnly ? DataSourceType.REPLICA : DataSourceType.PRIMARY;
        DataSourceContextHolder.set(target);
        try {
            return pjp.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }
}
