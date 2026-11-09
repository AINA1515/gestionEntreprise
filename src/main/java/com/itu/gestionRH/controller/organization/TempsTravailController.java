package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.entity.organization.*;
import com.itu.gestionRH.repository.organization.*;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.organization.TempsTravailService;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/temps-travail")
public class TempsTravailController {
    private final RegimeTravailRepository regimes;
    private final PlanningTravailRepository plannings;
    private final CreneauTravailRepository creneaux;
    private final EmployeRepository employes;
    private final UtilisateurRepository utilisateurs;
    private final AffectationRepository affectations;
    private final AccessScope scope;
    private final TempsTravailService tempsService;

    public TempsTravailController(RegimeTravailRepository r, PlanningTravailRepository p, CreneauTravailRepository c,
            EmployeRepository e, UtilisateurRepository u, AffectationRepository a, AccessScope s,
            TempsTravailService t) {
        regimes = r;
        plannings = p;
        creneaux = c;
        employes = e;
        utilisateurs = u;
        affectations = a;
        scope = s;
        tempsService = t;
    }

    public record PlanningRequest(Long employeId, Long regimeId, LocalDate dateDebut, LocalDate dateFin, String motif) {
    }

    public record CreneauRequest(Long planningId, LocalDateTime dateDebut, LocalDateTime dateFin, String typeCreneau,
            String libelle) {
    }

    public record StatutCreneauRequest(String statut) {
    }
    public record DeclarationCreneauRequest(String statutEmploye, String commentaireEmploye) {}

    @GetMapping("/regimes")
    @PreAuthorize("isAuthenticated()")
    public List<RegimeTravail> regimes() {
        return regimes.findAll();
    }

    @GetMapping("/employes/{id}/plannings")
    @PreAuthorize("isAuthenticated()")
    public List<PlanningTravail> plannings(@PathVariable Long id) {
        return plannings.findByEmployeIdOrderByDateDebutDesc(id);
    }

    @GetMapping("/employes/{id}/creneaux")
    @PreAuthorize("isAuthenticated()")
    public List<CreneauTravail> creneaux(@PathVariable Long id, @RequestParam LocalDateTime debut,
            @RequestParam LocalDateTime fin) {
        return creneaux.findByPlanningEmployeIdAndDateDebutBetweenOrderByDateDebutAsc(id, debut, fin);
    }

    @GetMapping("/plannings/{id}/creneaux")
    @PreAuthorize("isAuthenticated()")
    public List<CreneauTravail> creneauxPlanning(@PathVariable Long id) {
        return creneaux.findByPlanningIdOrderByDateDebutAsc(id);
    }

    @PostMapping("/plannings")
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public PlanningTravail creer(@RequestBody PlanningRequest r) {
        return tempsService.creerPlanning(r.employeId(), r.regimeId(), r.dateDebut(), r.dateFin(), r.motif());
    }

    @PutMapping("/plannings/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public PlanningTravail modifierPlanning(@PathVariable Long id, @RequestBody PlanningRequest r) {
        return tempsService.modifierPlanning(id, r.regimeId(), r.dateDebut(), r.dateFin(), r.motif());
    }

    @DeleteMapping("/plannings/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public void supprimerPlanning(@PathVariable Long id) {
        tempsService.supprimerPlanning(id);
    }

    @PostMapping("/creneaux")
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public CreneauTravail creerCreneau(@RequestBody CreneauRequest r) {
        return tempsService.creerCreneau(r.planningId(), r.dateDebut(), r.dateFin(), r.typeCreneau(), r.libelle());
    }

    @PutMapping("/creneaux/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public CreneauTravail modifierCreneau(@PathVariable Long id, @RequestBody CreneauRequest r) {
        return tempsService.modifierCreneau(id, r.dateDebut(), r.dateFin(), r.typeCreneau(), r.libelle());
    }

    @PatchMapping("/creneaux/{id}/statut")
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public CreneauTravail changerStatut(@PathVariable Long id, @RequestBody StatutCreneauRequest r) {
        return tempsService.changerStatutCreneau(id, r.statut());
    }

    @PatchMapping("/creneaux/{id}/declaration")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public CreneauTravail declarer(@PathVariable Long id, @RequestBody DeclarationCreneauRequest r) {
        CreneauTravail c = creneaux.findById(id).orElseThrow();
        Long courant = utilisateurs.findById(scope.currentUserId()).map(u -> u.getEmployeId()).orElse(null);
        if (courant == null || c.getPlanning().getEmploye().getId() == null || !courant.equals(c.getPlanning().getEmploye().getId()))
            throw new org.springframework.security.access.AccessDeniedException("Seul le salarié concerné peut déclarer ce créneau");
        c.setStatutEmploye(r.statutEmploye()); c.setCommentaireEmploye(r.commentaireEmploye());
        c.setDateDeclaration(LocalDateTime.now()); c.setStatutValidation("EN_ATTENTE");
        return creneaux.save(c);
    }

    @PatchMapping("/creneaux/{id}/validation")
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    @Transactional
    public CreneauTravail valider(@PathVariable Long id, @RequestBody StatutCreneauRequest r) {
        CreneauTravail c = creneaux.findById(id).orElseThrow();
        Long courant = utilisateurs.findById(scope.currentUserId()).map(u -> u.getEmployeId()).orElse(null);
        c.setStatutValidation(r.statut()); c.setDateValidation(LocalDateTime.now());
        if (courant != null) c.setValidateur(employes.findById(courant).orElse(null));
        return creneaux.save(c);
    }
    @GetMapping("/creneaux/a-valider")
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public List<CreneauTravail> travauxAValider() { return creneaux.findAll(); }

    @DeleteMapping("/creneaux/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public void supprimerCreneau(@PathVariable Long id) {
        tempsService.supprimerCreneau(id);
    }
}
