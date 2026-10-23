package com.itu.gestionRH.controller.recruitment;

import java.util.List;
import com.itu.gestionRH.entity.enums.CandidatureStatut;
import com.itu.gestionRH.entity.enums.EvaluationDecision;
import com.itu.gestionRH.entity.recruitment.EvaluationCandidat;
import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.enums.PropositionStatut;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.workflow.CandidatureMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.repository.recruitment.EntretienRepository;
import com.itu.gestionRH.repository.recruitment.EvaluationCandidatRepository;
import com.itu.gestionRH.repository.hiring.PropositionEmbaucheRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.workflow.PropositionEmbaucheMachine;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.security.jwt.JwtAuthenticationFilter.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Évaluation des candidats après entretien par le manager de la demande. */
@RestController
@RequestMapping("/api/v1/recrutement")
public class EvaluationCandidatController {
    private final AccessScope accessScope;
    private final EntretienRepository entretiens;
    private final EvaluationCandidatRepository evaluations;
    private final CandidatureRepository candidatures;
    private final CandidatureMachine candidatureMachine;
    private final PropositionEmbaucheRepository propositions;
    private final PropositionEmbaucheMachine propositionMachine;
    private final UtilisateurRepository utilisateurs;

    public EvaluationCandidatController(AccessScope accessScope, EntretienRepository entretiens,
            EvaluationCandidatRepository evaluations, CandidatureRepository candidatures, CandidatureMachine candidatureMachine,
            PropositionEmbaucheRepository propositions, PropositionEmbaucheMachine propositionMachine, UtilisateurRepository utilisateurs) {
        this.accessScope = accessScope;
        this.entretiens = entretiens;
        this.evaluations = evaluations;
        this.candidatures = candidatures;
        this.candidatureMachine = candidatureMachine;
        this.propositions = propositions;
        this.propositionMachine = propositionMachine;
        this.utilisateurs = utilisateurs;
    }

    public record DecisionRequest(@NotBlank @Pattern(regexp = "FAVORABLE|DEFAVORABLE|RESERVE") String decision,
                                  @NotBlank String observations, ContratType typeContrat,
                                  java.math.BigDecimal salaireBrut, java.time.LocalDate dateDebut,
                                  java.time.LocalDate dateLimiteReponse) {}
    public record DecisionResponse(Long id, Long candidatureId, Long entretienId, String decision,
                                  String observations, Long propositionId) {}

    @PreAuthorize("hasAuthority('PERM_EVALUATION_SAISIR')")
    @PostMapping("/candidatures/{id}/evaluations")
    @Transactional
    public DecisionResponse enregistrer(@PathVariable Long id, @Valid @RequestBody DecisionRequest request) {
        var candidature = accessScope.requireCandidature(id);
        Long managerId = candidature.getOffre().getDemande().getManager() == null ? null
                : candidature.getOffre().getDemande().getManager().getId();
        Long currentId = currentUserId();
        if (managerId == null || !managerId.equals(currentId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable.");
        if (candidature.getStatutCandidature() != CandidatureStatut.EN_ENTRETIEN)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette candidature n’est pas en cours d’entretien.");
        if ("FAVORABLE".equals(request.decision()) && (request.typeContrat() == null
                || request.dateLimiteReponse() == null || request.dateLimiteReponse().isBefore(java.time.LocalDate.now())
                || (request.salaireBrut() != null && request.salaireBrut().signum() < 0)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pour une décision favorable, indiquez le contrat et une date limite de réponse valide.");
        var entretien = entretiens.findByCandidatureOffreDemandeManagerIdOrderByDateEntretienAsc(managerId).stream()
                .filter(e -> id.equals(e.getCandidature().getId()) && e.getEvaluation() == null)
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Aucun entretien à évaluer pour cette candidature."));
        EvaluationCandidat evaluation = EvaluationCandidat.nouvelle();
        evaluation.setEntretien(entretien);
        evaluation.setDecision(EvaluationDecision.valueOf(request.decision()));
        evaluation.setCommentaires(request.observations().trim());
        entretien.setStatutEntretien(com.itu.gestionRH.entity.enums.EntretienStatut.REALISE);
        entretien.setEvaluation(evaluation);
        var entretiensCandidature = candidature.getEntretiens();
        if (entretiensCandidature != null && !entretiensCandidature.contains(entretien)) entretiensCandidature.add(entretien);
        var saved = evaluations.save(evaluation);
        if (saved.getDecision() == EvaluationDecision.FAVORABLE)
            candidatureMachine.transition(candidature, "retenir", WorkflowContext.utilisateurCourant(request.observations()));
        else if (saved.getDecision() == EvaluationDecision.DEFAVORABLE) {
            candidature.setMotifRejet(request.observations().trim());
            candidatureMachine.transition(candidature, "rejeter", WorkflowContext.utilisateurCourant(request.observations()));
        }
        PropositionEmbauche proposition = null;
        if (saved.getDecision() == EvaluationDecision.FAVORABLE) {
            proposition = PropositionEmbauche.nouvelle();
            proposition.setCandidature(candidature);
            proposition.setRedacteur(utilisateurs.findById(currentId).orElseThrow());
            proposition.setTypeContratPropose(request.typeContrat());
            proposition.setSalaireBrutPropose(request.salaireBrut());
            proposition.setDateDebutSouhaitee(request.dateDebut());
            proposition.setDateEnvoi(java.time.LocalDate.now());
            proposition.setDateLimiteReponse(request.dateLimiteReponse());
            proposition.appliquerTransitionWorkflow(PropositionStatut.BROUILLON);
            propositions.save(proposition);
            propositionMachine.transition(proposition, "envoyer", WorkflowContext.utilisateurCourant("Évaluation favorable : proposition d’embauche"));
        }
        return new DecisionResponse(saved.getId(), id, entretien.getId(), saved.getDecision().name(), saved.getCommentaires(), proposition == null ? null : proposition.getId());
    }

    @PreAuthorize("hasAuthority('PERM_EVALUATION_SAISIR')")
    @GetMapping("/candidatures/{id}/evaluations")
    @Transactional(readOnly = true)
    public List<DecisionResponse> lister(@PathVariable Long id) {
        var candidature = accessScope.requireCandidature(id);
        Long managerId = candidature.getOffre().getDemande().getManager() == null ? null
                : candidature.getOffre().getDemande().getManager().getId();
        if (managerId == null || !managerId.equals(currentUserId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable.");
        return entretiens.findByCandidatureOffreDemandeManagerIdOrderByDateEntretienAsc(managerId).stream()
                .filter(e -> id.equals(e.getCandidature().getId()) && e.getEvaluation() != null)
                .map(e -> new DecisionResponse(e.getEvaluation().getId(), id, e.getId(), e.getEvaluation().getDecision().name(), e.getEvaluation().getCommentaires(),
                        propositions.findByCandidatureId(id) == null ? null : propositions.findByCandidatureId(id).getId())).toList();
    }

    private Long currentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal ? principal.userId() : null;
    }
}
