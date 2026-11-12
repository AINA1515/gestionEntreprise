package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.entity.organization.PointageJournalier;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.organization.*;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import java.time.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/pointages")
public class PointageController {
    private final PointageJournalierRepository pointages;
    private final EmployeRepository employes;
    private final UtilisateurRepository utilisateurs;
    private final AffectationRepository affectations;
    private final AccessScope scope;

    public PointageController(PointageJournalierRepository p, EmployeRepository e, UtilisateurRepository u,
            AffectationRepository a, AccessScope s) {
        pointages = p;
        employes = e;
        utilisateurs = u;
        affectations = a;
        scope = s;
    }

    public record PointageRequest(@NotNull Long employeId, @NotNull LocalDate datePointage, LocalDateTime entreePrevue,
            LocalDateTime sortiePrevue, LocalDateTime entreeReelle, LocalDateTime sortieReelle, Integer minutesRetard,
            Integer minutesHeuresSup, String statut) {
    }

    private Long eid() {
        return utilisateurs.findById(scope.currentUserId()).map(u -> u.getEmployeId()).orElse(null);
    }

    private boolean global() {
        return scope.hasPermission("DOCUMENT_CONSULTER_TOUS") || scope.hasPermission("CONTRAT_GERER");
    }

    private boolean equipe(Long cible, Long chef) {
        return chef != null && affectations.existsByEmployeIdAndSuperieurIdAndDateFinIsNull(cible, chef);
    }

    private void deny() {
        throw new org.springframework.security.access.AccessDeniedException("Accès limité à votre équipe");
    }

    @GetMapping("/employes/{id}")
    @PreAuthorize("isAuthenticated()")
    public List<PointageJournalier> lister(@PathVariable Long id, @RequestParam LocalDate debut,
            @RequestParam LocalDate fin) {
        Long c = eid();
        if (!global() && !id.equals(c) && !equipe(id, c))
            deny();
        return pointages.findByEmployeIdAndDatePointageBetweenOrderByDatePointageAsc(id, debut, fin);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public List<PointageJournalier> dashboard(@RequestParam LocalDate debut, @RequestParam LocalDate fin) {
        if (global())
            return pointages.findByDatePointageBetweenOrderByDatePointageAsc(debut, fin);
        Long c = eid();
        if (c != null && scope.hasPermission("DEMANDE_CREER")) {
            List<Long> ids = affectations.findBySuperieurIdAndDateFinIsNull(c).stream().map(a -> a.getEmploye().getId())
                    .toList();
            if (!ids.isEmpty())
                return pointages.findByEmployeIdInAndDatePointageBetweenOrderByDatePointageAsc(ids, debut, fin);
        }
        return c == null ? List.of()
                : pointages.findByEmployeIdAndDatePointageBetweenOrderByDatePointageAsc(c, debut, fin);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public PointageJournalier saisir(@Valid @RequestBody PointageRequest r) {
        Long c = eid();
        if (!global() && !equipe(r.employeId(), c))
            deny();
        var p = pointages.findByEmployeIdAndDatePointage(r.employeId(), r.datePointage())
                .orElseGet(PointageJournalier::new);
        p.setEmploye(employes.findById(r.employeId()).orElseThrow());
        p.setDatePointage(r.datePointage());
        p.setEntreePrevue(r.entreePrevue());
        p.setSortiePrevue(r.sortiePrevue());
        p.setEntreeReelle(r.entreeReelle());
        p.setSortieReelle(r.sortieReelle());
        int retard = 0, sup = 0;
        if (r.entreePrevue() != null && r.entreeReelle() != null)
            retard = (int) Math.max(0, java.time.Duration.between(r.entreePrevue(), r.entreeReelle()).toMinutes());
        if (r.sortiePrevue() != null && r.sortieReelle() != null)
            sup = (int) Math.max(0, java.time.Duration.between(r.sortiePrevue(), r.sortieReelle()).toMinutes());
        p.setMinutesRetard(retard);
        p.setMinutesHeuresSup(sup);
        p.setStatut(r.statut() == null ? (retard > 0 ? "RETARD" : "PRESENT") : r.statut());
        p.setSaisiPar(c == null ? null : employes.findById(c).orElse(null));
        return pointages.save(p);
    }
}
