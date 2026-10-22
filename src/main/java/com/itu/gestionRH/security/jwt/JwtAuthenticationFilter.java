package com.itu.gestionRH.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final JwtAuthenticationEntryPoint entryPoint;
    public JwtAuthenticationFilter(JwtService jwtService, JwtAuthenticationEntryPoint entryPoint) {
        this.jwtService = jwtService; this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                Number uid = claims.get("uid", Number.class);
                List<String> roles = stringList(claims.get("roles"));
                List<String> permissions = stringList(claims.get("perms"));
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority("PERM_" + permission)));
                var principal = new JwtPrincipal(uid.longValue(), claims.getSubject());
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, authorities));
            } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
                SecurityContextHolder.clearContext();
                entryPoint.commence(request, response, new BadCredentialsException("Jeton invalide ou expiré.", ex));
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
    }

    public record JwtPrincipal(Long userId, String login) implements java.security.Principal {
        @Override public String getName() { return login; }
    }
}
