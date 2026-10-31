package com.itu.gestionRH.controller.recruitment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.gestionRH.entity.recruitment.Candidature;
import com.itu.gestionRH.repository.hiring.PropositionEmbaucheRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.security.CandidatService;

/** Fiche complète d'une candidature : candidat, CV, entretiens et évaluations, proposition. */
@RestController
@RequestMapping("/api/v1/recrutement/candidatures")
public class CandidatureDetailController {
    public record EntretienDetail(Long id, LocalDate date, String type, String lieu, String statut,
            String decision, String observations) {}
    public record PropositionDetail(Long id, String statut, String typeContrat, BigDecimal salaireBrut,
            LocalDate dateDebut, LocalDate dateFin, LocalDate dateLimiteReponse, LocalDate dateReponse, String motifRefus) {}
    public record CandidatureDetail(Long id, String statut, LocalDate datePostulation, BigDecimal salairePropose,
            String lettreMotivation, String motifRejet, Long offreId, String offreTitre, Long demandeId,
            String typeEmploi, String niveauRequis, BigDecimal budget,
            Long candidatId, String candidatNom, String candidatEmail, String candidatTelephone,
            CandidatService.CvDto cv, List<EntretienDetail> entretiens, PropositionDetail proposition) {}

    private final AccessScope accessScope;
    private final PropositionEmbaucheRepository propositions;
    private final ObjectMapper objectMapper;

    public CandidatureDetailController(AccessScope accessScope, PropositionEmbaucheRepository propositions, ObjectMapper objectMapper) {
        this.accessScope = accessScope;
        this.propositions = propositions;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("hasAnyAuthority('PERM_CANDIDATURE_CONSULTER','PERM_EVALUATION_SAISIR')")
    @GetMapping("/{id:\\d+}/detail")
    @Transactional(readOnly = true)
    public CandidatureDetail detail(@PathVariable Long id) {
        Candidature c = accessScope.requireCandidature(id);
        // Comme pour le profil : le manager ne voit la fiche qu'une fois la candidature transmise par la RH.
        if (!accessScope.hasPermission("CANDIDATURE_PRESELECTIONNER")
                && (c.getStatutCandidature() == com.itu.gestionRH.entity.enums.CandidatureStatut.RECUE
                || c.getStatutCandidature() == com.itu.gestionRH.entity.enums.CandidatureStatut.ANNULEE))
            throw new com.itu.gestionRH.common.NotFoundException("Candidature introuvable.");
        var offre = c.getOffre();
        var demande = offre == null ? null : offre.getDemande();
        var candidat = c.getCandidat();

        CandidatService.CvDto cv = null;
        if (candidat != null && candidat.getCvContenu() != null && !candidat.getCvContenu().isBlank()) {
            try { cv = objectMapper.readValue(candidat.getCvContenu(), CandidatService.CvDto.class); }
            catch (Exception ignored) { /* CV importé : disponible dans la GED, pas de contenu structuré. */ }
        }

        List<EntretienDetail> entretiens = c.getEntretiens() == null ? List.of() : c.getEntretiens().stream()
                .sorted(Comparator.comparing(com.itu.gestionRH.entity.recruitment.Entretien::getDateEntretien,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(e -> new EntretienDetail(e.getId(), e.getDateEntretien(),
                        e.getTypeEntretien() == null ? null : e.getTypeEntretien().name(), e.getLieu(),
                        e.getStatutEntretien() == null ? null : e.getStatutEntretien().name(),
                        e.getEvaluation() == null || e.getEvaluation().getDecision() == null ? null : e.getEvaluation().getDecision().name(),
                        e.getEvaluation() == null ? null : e.getEvaluation().getCommentaires()))
                .toList();

        var p = propositions.findByCandidatureId(id);
        PropositionDetail proposition = p == null ? null : new PropositionDetail(p.getId(), p.getStatutProposition().name(),
                p.getTypeContratPropose() == null ? null : p.getTypeContratPropose().name(), p.getSalaireBrutPropose(),
                p.getDateDebutSouhaitee(), p.getDateFinSouhaitee(), p.getDateLimiteReponse(), p.getDateReponse(), p.getMotifRefus());

        return new CandidatureDetail(c.getId(), c.getStatutCandidature().name(), c.getDatePostulation(), c.getSalairePropose(),
                c.getLettreMotivation(), c.getMotifRejet(), offre == null ? null : offre.getId(), offre == null ? null : offre.getTitre(),
                demande == null ? null : demande.getId(),
                demande == null || demande.getTypeEmploi() == null ? null : demande.getTypeEmploi().getLibelle(),
                demande == null || demande.getNiveauFormation() == null ? null : demande.getNiveauFormation().getLibelle(),
                demande == null ? null : demande.getBudgetAlloue(),
                candidat == null ? null : candidat.getId(),
                candidat == null ? null : candidat.getPrenom() + " " + candidat.getNom(),
                candidat == null ? null : candidat.getEmail(), candidat == null ? null : candidat.getTelephone(),
                cv, entretiens, proposition);
    }
}
