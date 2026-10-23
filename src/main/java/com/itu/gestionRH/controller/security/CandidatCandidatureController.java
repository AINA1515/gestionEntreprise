package com.itu.gestionRH.controller.security;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.entity.recruitment.Candidature;
import java.time.LocalDate;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/candidat/candidatures")
public class CandidatCandidatureController {
    private final AccessScope accessScope;
    private final CandidatureRepository candidatures;
    public CandidatCandidatureController(AccessScope accessScope, CandidatureRepository candidatures) { this.accessScope=accessScope; this.candidatures=candidatures; }
    public record CandidatureResponse(Long id, Long offreId, String poste, String statut, String datePostulation,
                                      BigDecimal salairePropose, BigDecimal salaireReference) {}
    @PreAuthorize("hasAnyAuthority('PERM_CANDIDATURE_POSTULER','PERM_CANDIDATURE_CONSULTER')")
    @GetMapping
    @Transactional(readOnly = true)
    public List<CandidatureResponse> lister() {
        Long candidatId=accessScope.requireCandidatCourant();
        return candidatures.findByCandidatId(candidatId, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream().map(this::versReponse).toList();
    }

    @PreAuthorize("hasAuthority('PERM_CANDIDATURE_POSTULER')")
    @DeleteMapping("/{id}")
    @Transactional
    public void annuler(@PathVariable Long id) {
        Long candidatId = accessScope.requireCandidatCourant();
        Candidature candidature = candidatures.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable."));
        if (candidature.getCandidat() == null || !candidatId.equals(candidature.getCandidat().getId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable.");
        if (candidature.getStatutCandidature() == com.itu.gestionRH.entity.enums.CandidatureStatut.REJETEE
                || candidature.getStatutCandidature() == com.itu.gestionRH.entity.enums.CandidatureStatut.ANNULEE
                || candidature.getStatutCandidature() == com.itu.gestionRH.entity.enums.CandidatureStatut.RETENUE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette candidature ne peut plus être annulée.");
        candidature.appliquerTransitionWorkflow(com.itu.gestionRH.entity.enums.CandidatureStatut.ANNULEE);
    }

    private CandidatureResponse versReponse(Candidature candidature) {
        return new CandidatureResponse(candidature.getId(), candidature.getOffre().getId(),
                candidature.getOffre().getTitre(), candidature.getStatutCandidature().name(),
                candidature.getDatePostulation().toString(), candidature.getSalairePropose(),
                candidature.getOffre().getDemande().getBudgetAlloue());
    }
}
