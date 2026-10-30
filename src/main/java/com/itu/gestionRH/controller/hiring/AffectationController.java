package com.itu.gestionRH.controller.hiring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.itu.gestionRH.entity.hiring.Affectation;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.hiring.AffectationService;
import com.itu.gestionRH.workflow.WorkflowContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Changement de poste d'un salarié déjà en poste (distinct de l'embauche d'un candidat). */
@RestController
@RequestMapping("/api/v1/affectations")
public class AffectationController {
    public record MutationRequest(@NotNull Long employeId, @NotNull Long nouveauPosteId, LocalDate dateEffet,
            @NotBlank @Size(max = 300) String motif, BigDecimal nouveauSalaire, Long superieurId) {}
    public record AffectationDto(Long id, Long employeId, Long posteId, String posteCode, String posteIntitule,
            Long departementId, String departement, Long contratId, Long superieurId, LocalDate dateDebut,
            LocalDate dateFin, String motif, boolean courante) {}

    private final AffectationService service;
    private final AccessScope accessScope;

    public AffectationController(AffectationService service, AccessScope accessScope) {
        this.service = service;
        this.accessScope = accessScope;
    }

    @PreAuthorize("hasAuthority('PERM_CONTRAT_GERER')")
    @PostMapping("/mutations")
    @Transactional
    public AffectationDto muter(@Valid @RequestBody MutationRequest r) {
        return dto(service.muter(r.employeId(), r.nouveauPosteId(), r.dateEffet(), r.motif(), r.nouveauSalaire(),
                r.superieurId(), WorkflowContext.utilisateurCourant(r.motif())));
    }

    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_DOCUMENT_CONSULTER_PERSO','PERM_DEMANDE_CREER')")
    @GetMapping("/employes/{employeId}")
    @Transactional(readOnly = true)
    public List<AffectationDto> historique(@PathVariable Long employeId) {
        accessScope.requireEmploye(employeId);
        return service.historique(employeId).stream().map(this::dto).toList();
    }

    private AffectationDto dto(Affectation a) {
        var p = a.getPoste();
        var d = p.getDepartement();
        return new AffectationDto(a.getId(), a.getEmploye().getId(), p.getId(), p.getCodePoste(), p.getIntitule(),
                d == null ? null : d.getId(), d == null ? null : d.getLibelle(),
                a.getContrat() == null ? null : a.getContrat().getId(),
                a.getSuperieur() == null ? null : a.getSuperieur().getId(),
                a.getDateDebut(), a.getDateFin(), a.getMotif(), a.getDateFin() == null);
    }
}
