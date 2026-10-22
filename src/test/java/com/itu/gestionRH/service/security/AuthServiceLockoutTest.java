package com.itu.gestionRH.service.security;

import static org.junit.jupiter.api.Assertions.*;

import com.itu.gestionRH.common.AuthException;
import com.itu.gestionRH.entity.enums.CompteStatut;
import com.itu.gestionRH.entity.enums.CompteType;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.security.*;
import com.itu.gestionRH.security.jwt.JwtService;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceLockoutTest {
    @Test void cinqMotsDePasseErronesVerrouillentEtJournalisentChaqueTentative() {
        var encoder = new BCryptPasswordEncoder();
        var user = Utilisateur.nouvelle();
        user.setLogin("agent"); user.setEmail("agent@example.test"); user.setTypeCompte(CompteType.INTERNE);
        user.setStatutCompte(CompteStatut.ACTIF); user.setNbEchecsConnexion(0);
        user.setMotDePasseHash(encoder.encode("CorrectPassword!123"));
        AtomicInteger logs = new AtomicInteger();
        UtilisateurRepository users = proxy(UtilisateurRepository.class, (method, args) -> switch (method) {
            case "findByLoginIgnoreCaseOrEmailIgnoreCase" -> Optional.of(user);
            case "save" -> args[0];
            default -> null;
        });
        LogConnexionRepository connectionLogs = proxy(LogConnexionRepository.class, (method, args) -> {
            if (method.equals("save")) { logs.incrementAndGet(); return args[0]; }
            return null;
        });
        var auth = new AuthService(users, null, null, null, null, connectionLogs, encoder,
                new JwtService("test-secret-that-is-at-least-32-bytes-long", 30));

        for (int attempt = 1; attempt <= 4; attempt++) {
            AuthException error = assertThrows(AuthException.class,
                    () -> auth.authentifier("agent", "BadPassword!123", "127.0.0.1", "test-agent"));
            assertEquals("INVALID_CREDENTIALS", error.getCode());
            assertEquals(CompteStatut.ACTIF, user.getStatutCompte());
            assertEquals(attempt, user.getNbEchecsConnexion());
        }
        AuthException fifth = assertThrows(AuthException.class,
                () -> auth.authentifier("agent", "BadPassword!123", "127.0.0.1", "test-agent"));
        assertEquals(HttpStatus.LOCKED, fifth.getStatus());
        assertEquals("ACCOUNT_LOCKED", fifth.getCode());
        assertEquals(CompteStatut.VERROUILLE, user.getStatutCompte());
        assertEquals(5, logs.get());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (object, method, args) -> {
            if (method.getDeclaringClass() == Object.class) return method.getName().equals("toString") ? type.getSimpleName() : null;
            return invocation.call(method.getName(), args == null ? new Object[0] : args);
        });
    }
    @FunctionalInterface private interface Invocation { Object call(String method, Object[] args); }
}
