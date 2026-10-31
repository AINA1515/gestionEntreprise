package com.itu.gestionRH.controller.hiring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.hiring.ContratSuiviService;
import com.itu.gestionRH.workflow.WorkflowContext;

/** Consultation et clôture des contrats, alertes de fin de CDD, contrats du salarié connecté. */
@RestController
@RequestMapping("/api/v1")
public class ContratSuiviController {
    public record ContratDetail(Long id, Long employeId, String employeNom, String employeMatricule, Long posteId,
            String posteIntitule, String typeContrat, String typeActe, Long contratPrecedentId, LocalDate dateDebut,
            LocalDate dateFin, LocalDate dateFinEffective, BigDecimal salaireBrut, String devise, String statut,
            Long joursRestants) {}
    public record TerminerContratRequest(LocalDate dateFin) {}

    private final ContratSuiviService service;
    private final ContratRepository contrats;
    private final AffectationRepository affectations;
    private final UtilisateurRepository utilisateurs;
    private final AccessScope accessScope;

    public ContratSuiviController(ContratSuiviService service, ContratRepository contrats,
            AffectationRepository affectations, UtilisateurRepository utilisateurs, AccessScope accessScope) {
        this.service = service;
        this.contrats = contrats;
        this.affectations = affectations;
        this.utilisateurs = utilisateurs;
        this.accessScope = accessScope;
    }

    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_DOCUMENT_CONSULTER_PERSO','PERM_DEMANDE_CREER')")
    @GetMapping("/onboarding/contrats/{id:\\d+}")
    @Transactional(readOnly = true)
    public ContratDetail obtenir(@PathVariable Long id) {
        Contrat contrat = service.obtenir(id);
        accessScope.requireEmploye(contrat.getEmploye().getId());
        return detail(contrat);
    }

    /** Alerte « CDD bientôt échu » : tous les CDD pour la RH, ceux du département pour un manager. */
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_DEMANDE_CREER')")
    @GetMapping("/onboarding/contrats/expirant")
    @Transactional(readOnly = true)
    public List<ContratDetail> expirant(@RequestParam(defaultValue = "30") int jours) {
        var liste = service.expirantBientot(jours);
        if (!accessScope.hasPermission("CONTRAT_GERER")) {
            Set<Long> visibles = employesDuDepartementDuManager();
            liste = liste.stream().filter(c -> visibles.contains(c.getEmploye().getId())).toList();
        }
        return liste.stream().map(this::detail).toList();
    }

    @PreAuthorize("hasAuthority('PERM_CONTRAT_GERER')")
    @PostMapping("/onboarding/contrats/{id:\\d+}/terminer")
    @Transactional
    public ContratDetail terminer(@PathVariable Long id, @RequestBody(required = false) TerminerContratRequest request) {
        LocalDate fin = request == null ? null : request.dateFin();
        return detail(service.terminer(id, fin, WorkflowContext.utilisateurCourant("Fin de contrat")));
    }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @GetMapping("/employes/moi/contrats")
    @Transactional(readOnly = true)
    public List<ContratDetail> mesContrats() {
        var utilisateur = utilisateurs.findById(accessScope.currentUserId())
                .orElseThrow(() -> new NotFoundException("Ressource introuvable."));
        Long employeId = utilisateur.getEmployeId();
        if (employeId == null) return List.of();
        return contrats.findByEmployeIdOrderByDateDebutDescIdDesc(employeId).stream().map(this::detail).toList();
    }

    private Set<Long> employesDuDepartementDuManager() {
        var manager = utilisateurs.findById(accessScope.currentUserId()).orElse(null);
        if (manager == null || manager.getEmployeId() == null) return Set.of();
        var affectation = affectations.findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(manager.getEmployeId()).orElse(null);
        if (affectation == null) return Set.of();
        Long departementId = affectation.getPoste().getDepartement().getId();
        return new java.util.HashSet<>(affectations.findEmployeIdsActifsByDepartementId(departementId));
    }

    private ContratDetail detail(Contrat c) {
        LocalDate fin = c.getDateFinEffective() != null ? c.getDateFinEffective() : c.getDateFin();
        Long jours = fin == null ? null : java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fin);
        var e = c.getEmploye();
        return new ContratDetail(c.getId(), e.getId(), e.getPrenom() + " " + e.getNom(), e.getMatricule(), c.getPoste().getId(),
                c.getPoste().getIntitule(), c.getTypeContrat().name(), c.getTypeActe().name(),
                c.getContratPrecedent() == null ? null : c.getContratPrecedent().getId(), c.getDateDebut(), c.getDateFin(),
                c.getDateFinEffective(), c.getSalaireBrut(), c.getDevise(), c.getStatutContrat().name(), jours);
    }
}
