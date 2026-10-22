package com.itu.gestionRH.controller.hiring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.itu.gestionRH.security.AccessScope;

@RestController
@RequestMapping("/api/v1/onboarding")
public class ContratController {
    private final AccessScope accessScope;
    public ContratController(AccessScope accessScope) { this.accessScope=accessScope; }
    public record ContratRequest(Long employeId, Long posteId, String typeContrat,
                                 LocalDate dateDebut, LocalDate dateFin, BigDecimal salaireBrut) {}
    public record ContratResponse(Long id, Long employeId, String typeContrat,
                                  LocalDate dateDebut, LocalDate dateFin, String statut) {}

    @PreAuthorize("hasAuthority('PERM_CONTRAT_GERER')")
    @PostMapping("/contrats")
    public ContratResponse creer(@RequestBody ContratRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Création de contrat à implémenter");
    }
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_DOCUMENT_CONSULTER_PERSO')")
    @GetMapping("/employes/{id}/contrats")
    public List<ContratResponse> historique(@PathVariable Long id) {
        accessScope.requireEmploye(id);
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Historique des contrats à implémenter");
    }
}
