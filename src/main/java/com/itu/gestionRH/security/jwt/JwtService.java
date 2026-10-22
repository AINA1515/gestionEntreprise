package com.itu.gestionRH.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-token-minutes:30}") long accessTokenMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("APP_JWT_SECRET doit contenir au moins 32 octets UTF-8.");
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMillis = Math.multiplyExact(accessTokenMinutes, 60_000L);
        if (expirationMillis <= 0) throw new IllegalArgumentException("La durée JWT doit être positive.");
    }

    public IssuedToken issue(Long userId, String login, List<String> roles, List<String> permissions) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMillis);
        String token = Jwts.builder().subject(login).claim("uid", userId).claim("roles", roles)
                .claim("perms", permissions).issuedAt(Date.from(now)).expiration(Date.from(expiresAt))
                .signWith(key).compact();
        return new IssuedToken(token, expiresAt);
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public record IssuedToken(String value, Instant expiresAt) {}
}
