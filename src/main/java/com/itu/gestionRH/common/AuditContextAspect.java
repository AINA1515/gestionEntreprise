package com.itu.gestionRH.common;

import java.sql.CallableStatement;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Sets Oracle CLIENT_IDENTIFIER once per Spring transaction and clears it before pool reuse. */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditContextAspect {
    private static final Object CONTEXT_KEY = new Object();
    private final JdbcTemplate jdbcTemplate;

    public AuditContextAspect(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Around("@annotation(org.springframework.transaction.annotation.Transactional) || " +
            "@within(org.springframework.transaction.annotation.Transactional)")
    public Object setClientIdentifier(ProceedingJoinPoint invocation) throws Throwable {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()
                && !TransactionSynchronizationManager.hasResource(CONTEXT_KEY)) {
            String login = currentLogin();
            jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                try (CallableStatement statement = connection.prepareCall("{ call DBMS_SESSION.SET_IDENTIFIER(?) }") ) {
                    statement.setString(1, login);
                    statement.execute();
                }
                return null;
            });
            TransactionSynchronizationManager.bindResource(CONTEXT_KEY, login);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    try {
                        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                            try (CallableStatement statement = connection.prepareCall("{ call DBMS_SESSION.CLEAR_IDENTIFIER }") ) {
                                statement.execute();
                            }
                            return null;
                        });
                    } finally {
                        if (TransactionSynchronizationManager.hasResource(CONTEXT_KEY)) {
                            TransactionSynchronizationManager.unbindResource(CONTEXT_KEY);
                        }
                    }
                }
            });
        }
        return invocation.proceed();
    }

    private String currentLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return "SYSTEME";
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) return "ANONYME";
        return name.length() <= 64 ? name : name.substring(0, 64);
    }
}
