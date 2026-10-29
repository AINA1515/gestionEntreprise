package com.itu.gestionRH.controller.onboarding;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.itu.gestionRH.entity.onboarding.Onboarding;
import com.itu.gestionRH.service.onboarding.OnboardingService;
import com.itu.gestionRH.security.AccessScope;
import java.util.List;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {
    public record TacheOnboardingDto(Long id, String intitule, String description, String serviceResponsable,
            boolean obligatoire, String statut, java.time.LocalDate dateLimite) {}
    public record InitialisationResponse(Long onboardingId, Long employeId, String statut,
            java.time.LocalDate dateDebut, java.time.LocalDate dateFinPrevue, List<TacheOnboardingDto> taches) {}
    private final OnboardingService service;
    private final AccessScope accessScope;
    public OnboardingController(OnboardingService service, AccessScope accessScope) { this.service = service; this.accessScope = accessScope; }
    @PreAuthorize("hasAuthority('PERM_ONBOARDING_ADMIN')")
    @PostMapping("/employes/{id}/initialiser-onboarding")
    public InitialisationResponse initialiser(@PathVariable Long id) {
        return dto(service.initialiser(id));
    }
    @PreAuthorize("hasAnyAuthority('PERM_ONBOARDING_ADMIN','PERM_DOCUMENT_CONSULTER_PERSO')")
    @GetMapping("/employes/{id}")
    public InitialisationResponse obtenir(@PathVariable Long id) {
        accessScope.requireEmploye(id);
        return dto(service.obtenir(id));
    }
    private InitialisationResponse dto(Onboarding onboarding) {
        return new InitialisationResponse(onboarding.getId(), onboarding.getEmploye().getId(),
                onboarding.getStatutGlobal().name(), onboarding.getDateDebut(), onboarding.getDateFinPrevue(),
                onboarding.getOnboardingTaches().stream().map(t -> new TacheOnboardingDto(t.getId(), t.getIntituleTache(),
                        t.getDescription(), t.getServiceResponsable().name(), Boolean.TRUE.equals(t.getEstObligatoire()),
                        t.getStatutTache().name(), t.getDateLimite())).toList());
    }
}
