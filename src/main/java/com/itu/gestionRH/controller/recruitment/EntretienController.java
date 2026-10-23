package com.itu.gestionRH.controller.recruitment;

import java.time.LocalDate;
import java.util.List;
import com.itu.gestionRH.entity.enums.CandidatureStatut;
import com.itu.gestionRH.entity.enums.EntretienStatut;
import com.itu.gestionRH.entity.enums.TypeEntretien;
import com.itu.gestionRH.entity.recruitment.Candidature;
import com.itu.gestionRH.entity.recruitment.Entretien;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.repository.recruitment.EntretienRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.workflow.CandidatureMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/recrutement")
public class EntretienController {

    public record PlanificationRequest(@NotNull LocalDate dateEntretien, TypeEntretien typeEntretien, String lieu) {}
    public record EntretienResponse(Long id, Long candidatureId, String candidat, String poste, LocalDate dateEntretien,
                                    String typeEntretien, String lieu, String statut, String decision, String commentaires) {}

    private final EntretienRepository entretiens;
    private final CandidatureRepository candidatures;
    private final AccessScope accessScope;
    private final CandidatureMachine candidatureMachine;

    public EntretienController(EntretienRepository entretiens, CandidatureRepository candidatures,
            AccessScope accessScope, CandidatureMachine candidatureMachine) {
        this.entretiens = entretiens;
        this.candidatures = candidatures;
        this.accessScope = accessScope;
        this.candidatureMachine = candidatureMachine;
    }

    @PreAuthorize("hasAnyAuthority('PERM_ENTRETIEN_PLANIFIER','ROLE_MANAGER')")
    @PostMapping("/candidatures/{id}/entretiens")
    @Transactional
    public EntretienResponse planifier(@PathVariable Long id, @Valid @RequestBody PlanificationRequest request) {
        Candidature candidature = accessScope.requireCandidature(id);
        if (candidature.getStatutCandidature() != CandidatureStatut.PRESELECTIONNEE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seule une candidature présélectionnée peut être planifiée.");
        if (request.dateEntretien().isBefore(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de l’entretien ne peut pas être passée.");
        var manager = candidature.getOffre().getDemande().getManager();
        if (manager == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun manager n’est associé à cette demande.");
        var entretien = Entretien.nouvelle();
        entretien.setCandidature(candidature);
        entretien.setIntervenant(manager);
        entretien.setDateEntretien(request.dateEntretien());
        entretien.setTypeEntretien(request.typeEntretien() == null ? TypeEntretien.RH : request.typeEntretien());
        entretien.setLieu(request.lieu());
        entretien.setStatutEntretien(EntretienStatut.PLANIFIE);
        candidatureMachine.transition(candidature, "planifier_entretien", WorkflowContext.utilisateurCourant(null));
        return versReponse(entretiens.save(entretien));
    }

    @PreAuthorize("hasAnyAuthority('PERM_EVALUATION_SAISIR','PERM_ENTRETIEN_PLANIFIER','ROLE_MANAGER')")
    @GetMapping("/entretiens")
    @Transactional(readOnly = true)
    public List<EntretienResponse> lister(@RequestParam(required = false) Long candidatureId) {
        List<Entretien> visibles = accessScope.hasPermission("PERM_ENTRETIEN_PLANIFIER")
                ? entretiens.findAll() : entretiens.findByCandidatureOffreDemandeManagerIdOrderByDateEntretienAsc(accessScope.currentUserId());
        return visibles.stream().filter(e -> candidatureId == null || candidatureId.equals(e.getCandidature().getId()))
                .map(this::versReponse).toList();
    }

    private EntretienResponse versReponse(Entretien e) {
        var candidat = e.getCandidature().getCandidat();
        String nom = candidat == null ? "Candidat" : ((candidat.getPrenom() == null ? "" : candidat.getPrenom() + " ")
                + (candidat.getNom() == null ? "" : candidat.getNom())).trim();
        var evaluation = e.getEvaluation();
        return new EntretienResponse(e.getId(), e.getCandidature().getId(), nom, e.getCandidature().getOffre().getTitre(),
                e.getDateEntretien(), e.getTypeEntretien().name(), e.getLieu(), e.getStatutEntretien().name(),
                evaluation == null ? null : evaluation.getDecision().name(), evaluation == null ? null : evaluation.getCommentaires());
    }
}
