package com.itu.gestionRH.controller.security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.itu.gestionRH.common.AuthException;
import com.itu.gestionRH.config.SecurityConfig;
import com.itu.gestionRH.security.jwt.JwtAuthenticationEntryPoint;
import com.itu.gestionRH.security.jwt.JwtAuthenticationFilter;
import com.itu.gestionRH.security.jwt.JwtService;
import com.itu.gestionRH.service.security.AuthService;
import java.time.Instant;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.itu.gestionRH.security.jwt.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

@WebMvcTest(UtilisateurController.class)
@Import({SecurityConfig.class, JwtService.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
@TestPropertySource(properties = "app.jwt.secret=integration-test-secret-at-least-32-characters-long-123456")
class UtilisateurControllerSecurityTest {
    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean AuthService authService;

    @Test void loginReussitEtRetourneLeJeton() throws Exception {
        var user = new AuthService.UserView(17L, "candidat", "candidat@example.test", "Camille Candidat",
                "CANDIDAT", List.of("CANDIDAT"), List.of("CANDIDATURE_LIRE"));
        when(authService.authentifier(eq("candidat"), eq("MotDePasse!123"), any(), any()))
                .thenReturn(new AuthService.LoginResult("jwt-test", Instant.parse("2026-10-22T18:00:00Z"), user));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifiant\":\"candidat\",\"motDePasse\":\"MotDePasse!123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt-test"))
                .andExpect(jsonPath("$.utilisateur.roles[0]").value("CANDIDAT"));
    }

    @Test void loginInvalideRenvoie401() throws Exception {
        when(authService.authentifier(any(), any(), any(), any()))
                .thenThrow(new AuthException("INVALID_CREDENTIALS", "Identifiants incorrects.", org.springframework.http.HttpStatus.UNAUTHORIZED));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifiant\":\"absent\",\"motDePasse\":\"faux\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Identifiants incorrects."));
    }

    @Test void ressourceProtegeeSansJetonRenvoie401() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test void jetonValideAutoriseUneRouteProtegee() throws Exception {
        var view = new AuthService.UserView(17L, "agent", "agent@example.test", "Agent RH",
                "INTERNE", List.of("GESTIONNAIRE_RH"), List.of("CONTRAT_GERER"));
        when(authService.utilisateurCourant(17L)).thenReturn(view);
        String token = jwtService.issue(17L, "agent", List.of("GESTIONNAIRE_RH"), List.of("CONTRAT_GERER")).value();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.login").value("agent"));
    }

    @Test void jetonFalsifieOuExpireRenvoie401() throws Exception {
        String token = jwtService.issue(17L, "agent", List.of(), List.of()).value();
        String falsifie = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + falsifie))
                .andExpect(status().isUnauthorized());

        var key = Keys.hmacShaKeyFor("integration-test-secret-at-least-32-characters-long-123456".getBytes(StandardCharsets.UTF_8));
        Instant expire = Instant.now().minus(Duration.ofMinutes(2));
        String expired = Jwts.builder().subject("agent").claim("uid", 17L).claim("roles", List.of())
                .claim("perms", List.of()).issuedAt(Date.from(expire.minusSeconds(60)))
                .expiration(Date.from(expire)).signWith(key).compact();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }
}
