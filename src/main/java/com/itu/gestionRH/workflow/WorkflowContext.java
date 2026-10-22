package com.itu.gestionRH.workflow;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record WorkflowContext(String acteur, Set<String> roles, Set<String> permissions,
                              String commentaire, Map<String, Object> attributs) {
    public WorkflowContext {
        acteur = acteur == null || acteur.isBlank() ? "SYSTEME" : acteur;
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        attributs = attributs == null ? Map.of() : Map.copyOf(attributs);
    }

    public static WorkflowContext of(String acteur, Set<String> roles, Set<String> permissions, String commentaire) {
        return new WorkflowContext(acteur, roles, permissions, commentaire, Map.of());
    }

    public static WorkflowContext utilisateurCourant(String commentaire) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return of("ANONYME", Set.of(), Set.of(), commentaire);
        }
        Set<String> authorities = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
        Set<String> roles = authorities.stream().filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length())).collect(Collectors.toSet());
        Set<String> permissions = authorities.stream().filter(a -> !a.startsWith("ROLE_")).collect(Collectors.toSet());
        return of(auth.getName(), roles, permissions, commentaire);
    }

    public boolean hasAccess(Set<String> rolesRequis, Set<String> permissionsRequises) {
        if (rolesRequis.isEmpty() && permissionsRequises.isEmpty()) return true;
        return roles.stream().anyMatch(rolesRequis::contains)
                || permissions.stream().anyMatch(permissionsRequises::contains);
    }
}
