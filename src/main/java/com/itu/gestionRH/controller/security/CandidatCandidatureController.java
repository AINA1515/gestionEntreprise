package com.itu.gestionRH.controller.security;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.itu.gestionRH.security.AccessScope;

@RestController
@RequestMapping("/api/v1/candidat/candidatures")
public class CandidatCandidatureController {
    private final AccessScope accessScope;
    public CandidatCandidatureController(AccessScope accessScope) { this.accessScope=accessScope; }
    public record CandidatureResponse(Long id, Long offreId, String poste, String statut, String datePostulation) {}
    @PreAuthorize("hasAnyAuthority('PERM_CANDIDATURE_POSTULER','PERM_CANDIDATURE_CONSULTER')")
    @GetMapping
    public List<CandidatureResponse> lister() {
        accessScope.requireCandidatCourant();
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Suivi des candidatures à implémenter");
    }
}
