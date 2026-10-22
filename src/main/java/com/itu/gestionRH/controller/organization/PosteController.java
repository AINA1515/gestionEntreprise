package com.itu.gestionRH.controller.organization;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.workflow.PosteMachine;
import com.itu.gestionRH.workflow.WorkflowContext;

@RestController
@RequestMapping("/api/v1/postes")
public class PosteController {

    public record PosteResponse(Long id, String codePoste, String intitule,
                                Long departementId, String departement, String statut, List<String> actionsDisponibles) {}

    private final PosteRepository posteRepository;
    private final PosteMachine machine;

    public PosteController(PosteRepository posteRepository, PosteMachine machine) {
        this.posteRepository = posteRepository;
        this.machine = machine;
    }

    /** Liste des postes, filtrable par departement (?departementId=). */
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DEMANDE_CONSULTER','PERM_CANDIDATURE_POSTULER','PERM_CANDIDATURE_CONSULTER','PERM_CONTRAT_GERER','PERM_ONBOARDING_ADMIN','PERM_UTILISATEUR_ADMIN')")
    @GetMapping
    @Transactional(readOnly = true)
    public List<PosteResponse> lister(@RequestParam(required = false) Long departementId) {
        return posteRepository.findAll().stream()
                .filter(p -> departementId == null
                        || (p.getDepartement() != null && departementId.equals(p.getDepartement().getId())))
                .map(p -> new PosteResponse(
                        p.getId(), p.getCodePoste(), p.getIntitule(),
                        p.getDepartement() != null ? p.getDepartement().getId() : null,
                        p.getDepartement() != null ? p.getDepartement().getLibelle() : null,
                        p.getStatutPoste().name(), machine.actionsDisponibles(p, WorkflowContext.utilisateurCourant(null))))
                .toList();
    }
}
