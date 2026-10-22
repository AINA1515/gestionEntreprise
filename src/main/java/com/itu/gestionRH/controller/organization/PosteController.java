package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.controller.organization.OrganisationDtos.PosteDto;
import com.itu.gestionRH.controller.organization.OrganisationDtos.PosteRequest;
import com.itu.gestionRH.entity.enums.PosteStatut;
import com.itu.gestionRH.service.organization.PosteService;
import com.itu.gestionRH.workflow.WorkflowContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/postes")
public class PosteController {
    public record TransitionRequest(@NotBlank String action, String commentaire) {}
    private final PosteService service;
    public PosteController(PosteService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DEMANDE_CONSULTER','PERM_CANDIDATURE_CONSULTER','PERM_CONTRAT_GERER','PERM_OFFRE_PUBLIER','PERM_UTILISATEUR_ADMIN')")
    public PageResponse<PosteDto> lister(@RequestParam(required=false) Long departementId,
            @RequestParam(required=false) PosteStatut statut, @RequestParam(required=false) String recherche,
            @PageableDefault(size=20, sort="intitule") Pageable pageable) {
        return PageResponse.from(service.lister(departementId, statut, recherche, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DEMANDE_CONSULTER','PERM_CANDIDATURE_CONSULTER','PERM_CONTRAT_GERER','PERM_OFFRE_PUBLIER','PERM_UTILISATEUR_ADMIN')")
    public PosteDto obtenir(@PathVariable Long id) { return service.obtenir(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_UTILISATEUR_ADMIN')")
    public PosteDto creer(@Valid @RequestBody PosteRequest request) { return service.creer(request); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_UTILISATEUR_ADMIN')")
    public PosteDto modifier(@PathVariable Long id, @Valid @RequestBody PosteRequest request) { return service.modifier(id, request); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_UTILISATEUR_ADMIN')")
    public PosteDto supprimer(@PathVariable Long id) { return service.fermer(id); }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_UTILISATEUR_ADMIN')")
    public PosteDto transitionner(@PathVariable Long id, @Valid @RequestBody TransitionRequest request) {
        service.transition(id, request.action(), WorkflowContext.utilisateurCourant(request.commentaire()));
        return service.obtenir(id);
    }
}
