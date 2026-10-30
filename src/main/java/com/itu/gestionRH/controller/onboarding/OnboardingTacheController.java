package com.itu.gestionRH.controller.onboarding;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.enums.TacheStatut;
import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import com.itu.gestionRH.repository.onboarding.OnboardingTacheRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.onboarding.OnboardingTacheService;
import com.itu.gestionRH.workflow.OnboardingTacheMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import java.util.Comparator;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/onboarding/taches")
public class OnboardingTacheController {
    private final OnboardingTacheRepository repository;
    private final OnboardingTacheMachine machine;
    private final OnboardingTacheService service;
    private final AccessScope accessScope;
    public OnboardingTacheController(OnboardingTacheRepository repository, OnboardingTacheMachine machine,
            OnboardingTacheService service, AccessScope accessScope) {
        this.repository=repository;this.machine=machine;this.service=service;this.accessScope=accessScope;
    }
    public record TacheResponse(Long id, String intitule, String statut, String serviceResponsable,
                                java.time.LocalDate dateLimite, List<String> actionsDisponibles, String justificatifRequis) {}
    public record MiseAJourTacheRequest(String statut, String commentaire) {}
    @PreAuthorize("hasAuthority('PERM_TACHE_TRAITER')")
    @GetMapping("/mes-taches")
    @Transactional(readOnly=true)
    public List<TacheResponse> mesTaches() {
        Long userId=accessScope.currentUserId();
        return repository.findByAssigneeAId(userId,org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .sorted(Comparator.comparing(OnboardingTache::getDateLimite,Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toResponse).toList();
    }
    @PreAuthorize("hasAuthority('PERM_TACHE_TRAITER')")
    @PatchMapping("/{id}")
    public TacheResponse mettreAJour(@PathVariable Long id, @RequestBody MiseAJourTacheRequest request) {
        OnboardingTache task=repository.findById(id).orElseThrow(()->new NotFoundException("Tâche introuvable."));
        accessScope.requireTaskOwner(task);
        TacheStatut statut;
        try { statut=TacheStatut.valueOf(request.statut()); }
        catch (RuntimeException ex) { throw new com.itu.gestionRH.common.BusinessRuleException("STATUT_TACHE_INVALIDE","Le statut de tâche demandé est invalide.",org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY); }
        String action=switch(statut) {
            case EN_COURS -> "commencer";
            case VALIDEE -> "valider";
            case A_FAIRE -> throw new com.itu.gestionRH.common.BusinessRuleException("TACHE_TRANSITION_INTERDITE","Une tâche ne peut pas revenir à l'état A_FAIRE.");
        };
        service.transition(task,action,WorkflowContext.utilisateurCourant(request.commentaire()));
        return toResponse(task);
    }

    private TacheResponse toResponse(OnboardingTache task){return new TacheResponse(task.getId(),task.getIntituleTache(),task.getStatutTache().name(),
            task.getServiceResponsable().name(),task.getDateLimite(),machine.actionsDisponibles(task,WorkflowContext.utilisateurCourant(null)),
            OnboardingTacheService.justificatifRequis(task)==null?null:OnboardingTacheService.justificatifRequis(task).name());}
}
