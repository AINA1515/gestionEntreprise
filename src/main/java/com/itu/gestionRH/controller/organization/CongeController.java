package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.entity.organization.*;
import com.itu.gestionRH.repository.organization.*;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/conges")
public class CongeController {
    final DemandeCongeRepository demandes;
    final CongeSoldeRepository soldes;
    final EmployeRepository employes;
    final UtilisateurRepository users;
    final AccessScope scope;

    public CongeController(DemandeCongeRepository d, CongeSoldeRepository s, EmployeRepository e,
            UtilisateurRepository u, AccessScope a) {
        demandes = d;
        soldes = s;
        employes = e;
        users = u;
        scope = a;
    }

    public record Demande(Long employeId, String typeConge, LocalDate dateDebut, LocalDate dateFin,
            double joursDemandes, String motif) {
    }

    public record Decision(String statut, String commentaire) {
    }

    private Long eid() {
        return users.findById(scope.currentUserId()).map(x -> x.getEmployeId()).orElse(null);
    }

    private double dispo(Long id) {
        return soldes.findByEmployeIdOrderByAnneeAsc(id).stream().mapToDouble(CongeSolde::getDisponible).sum();
    }

    @GetMapping("/solde")
    @PreAuthorize("isAuthenticated()")
    public List<CongeSolde> solde() {
        return soldes.findByEmployeIdOrderByAnneeAsc(eid());
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("isAuthenticated()")
    public List<DemandeConge> mes() {
        return demandes.findByEmployeIdOrderByDateDebutDesc(eid());
    }

    @GetMapping("/a-valider")
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public List<DemandeConge> validerListe() {
        return demandes.findByStatutOrderByDateDebutAsc("EN_ATTENTE");
    }

    @PostMapping
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public DemandeConge demander(@RequestBody Demande r) {
        if (r.joursDemandes() <= 0 || dispo(r.employeId()) < r.joursDemandes())
            throw new IllegalArgumentException("Solde de congés insuffisant");
        DemandeConge d = new DemandeConge();
        d.setEmploye(employes.findById(r.employeId()).orElseThrow());
        d.setTypeConge(r.typeConge());
        d.setDateDebut(r.dateDebut());
        d.setDateFin(r.dateFin());
        d.setJoursDemandes(r.joursDemandes());
        d.setMotif(r.motif());
        return demandes.save(d);
    }

    @PatchMapping("/{id}/decision")
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public DemandeConge decision(@PathVariable Long id, @RequestBody Decision r) {
        DemandeConge d = demandes.findById(id).orElseThrow();
        d.setStatut(r.statut());
        d.setCommentaireValidation(r.commentaire());
        d.setDateValidation(LocalDateTime.now());
        Long v = eid();
        if (v != null)
            d.setValidateur(employes.findById(v).orElse(null));
        if ("VALIDEE".equals(r.statut())) {
            double reste = d.getJoursDemandes();
            for (CongeSolde s : soldes.findByEmployeIdOrderByAnneeAsc(d.getEmploye().getId())) {
                double n = Math.min(reste, s.getDisponible());
                s.setJoursUtilises(s.getJoursUtilises() + n);
                soldes.save(s);
                reste -= n;
                if (reste <= 0)
                    break;
            }
        }
        return demandes.save(d);
    }
}
