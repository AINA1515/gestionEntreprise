package com.itu.gestionRH.controller.onboarding;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.enums.TacheStatut;
import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.onboarding.OnboardingTacheRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.organization.DepartementRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.service.onboarding.OnboardingService;
import com.itu.gestionRH.security.AccessScope;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manager/onboarding")
public class ManagerOnboardingController {
    private final OnboardingTacheRepository taches;
    private final UtilisateurRepository utilisateurs;
    private final AffectationRepository affectations;
    private final EmployeRepository employes;
    private final DepartementRepository departements;
    private final AccessScope accessScope;
    private final OnboardingService onboardingService;

    public ManagerOnboardingController(OnboardingTacheRepository taches,
            UtilisateurRepository utilisateurs, AffectationRepository affectations, EmployeRepository employes,
            DepartementRepository departements, AccessScope accessScope, OnboardingService onboardingService) {
        this.taches = taches; this.utilisateurs = utilisateurs;
        this.affectations = affectations; this.employes = employes; this.departements = departements;
        this.accessScope = accessScope;
        this.onboardingService = onboardingService;
    }

    public record Assignable(Long utilisateurId, Long employeId, String nom) {}
    public record EmployeOption(Long id, String matricule, String nom, String prenom) {}
    public record TacheManager(Long id, String intitule, String serviceResponsable, String statut,
            LocalDate dateLimite, Long assigneeId) {}
    public record OnboardingManager(Long employeId, List<TacheManager> taches, List<Assignable> assignables) {}
    public record AttributionRequest(Long utilisateurId) {}

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @org.springframework.web.bind.annotation.PostMapping("/employes/{employeId}/initialiser")
    @Transactional
    public OnboardingManager initialiser(@PathVariable Long employeId) {
        departementEmploye(employeId);
        onboardingService.initialiser(employeId);
        return tachesDuSalarie(employeId);
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @GetMapping("/employes")
    @Transactional(readOnly = true)
    public List<EmployeOption> employesDuDepartement() {
        Long departementId = departementManager();
        return employes.findActifsByDepartementId(departementId).stream()
                .map(e -> new EmployeOption(e.getId(), e.getMatricule(), e.getNom(), e.getPrenom())).toList();
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @GetMapping("/employes/{employeId}/taches")
    @Transactional(readOnly = true)
    public OnboardingManager tachesDuSalarie(@PathVariable Long employeId) {
        Long departementId = departementEmploye(employeId);
        var employeTaches = taches.findByOnboardingEmployeIdOrderByDateLimiteAscIdAsc(employeId);
        List<Assignable> options = utilisateurs.findActiveByDepartementId(departementId).stream()
                .map(u -> new Assignable(u.getId(), u.getEmployeId(), u.getNomAffichage())).toList();
        return new OnboardingManager(employeId, employeTaches.stream().map(this::dto).toList(), options);
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @PatchMapping("/taches/{tacheId}/assigner")
    @Transactional
    public TacheManager assigner(@PathVariable Long tacheId, @RequestBody AttributionRequest request) {
        if (request == null || request.utilisateurId() == null)
            throw new BusinessRuleException("RESPONSABLE_REQUIS", "Choisissez un salarié responsable de cette tâche.");
        OnboardingTache tache = taches.findById(tacheId).orElseThrow(() -> new NotFoundException("Tâche introuvable."));
        Long employeId = tache.getOnboarding().getEmploye().getId();
        Long departementId = departementEmploye(employeId);
        if (tache.getStatutTache() == TacheStatut.VALIDEE)
            throw new BusinessRuleException("TACHE_DEJA_VALIDEE", "Une tâche validée ne peut plus être réattribuée.");
        Utilisateur nouvelAssigne = utilisateurs.findById(request.utilisateurId())
                .filter(u -> u.getStatutCompte() == com.itu.gestionRH.entity.enums.CompteStatut.ACTIF)
                .orElseThrow(() -> new NotFoundException("Utilisateur à qui attribuer la tâche introuvable."));
        if (utilisateurs.findActiveByDepartementId(departementId).stream().noneMatch(u -> u.getId().equals(nouvelAssigne.getId())))
            throw new NotFoundException("Utilisateur à qui attribuer la tâche introuvable.");
        if (nouvelAssigne.getId().equals(tache.getAssigneeAId())) return dto(tache);
        // Le trigger TAH_ONBOARDING_TACHE clôture l'ancienne attribution et
        // écrit la nouvelle ligne d'historique à la mise à jour de ce champ.
        tache.setAssigneeAId(nouvelAssigne.getId());
        taches.saveAndFlush(tache);
        return dto(tache);
    }

    private TacheManager dto(OnboardingTache tache) {
        return new TacheManager(tache.getId(), tache.getIntituleTache(), tache.getServiceResponsable().name(),
                tache.getStatutTache().name(), tache.getDateLimite(), tache.getAssigneeAId());
    }

    private Long departementManager() {
        Utilisateur manager = utilisateurs.findById(accessScope.currentUserId())
                .orElseThrow(() -> new NotFoundException("Manager introuvable."));
        if (manager.getEmployeId() == null) throw new NotFoundException("Manager sans affectation de département.");
        var departementsResponsabilite = departements.findByResponsableIdAndEstActifTrue(manager.getEmployeId());
        if (!departementsResponsabilite.isEmpty()) return departementsResponsabilite.get(0).getId();
        return affectations.findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(manager.getEmployeId())
                .map(a -> a.getPoste().getDepartement().getId())
                .orElseThrow(() -> new NotFoundException("Aucun département actif n'est associé à ce manager."));
    }

    private Long departementEmploye(Long employeId) {
        Long departmentManager = departementManager();
        Long departmentSalarie = affectations.findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(employeId)
                .map(a -> a.getPoste().getDepartement().getId())
                .orElseThrow(() -> new NotFoundException("Affectation du salarié introuvable."));
        if (!departmentManager.equals(departmentSalarie)) throw new NotFoundException("Salarié hors du département du manager.");
        return departmentSalarie;
    }
}
