package com.itu.gestionRH.controller.recruitment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.workflow.DemandeRecrutementMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.security.AccessScope;

/** Espace manager : tableau de bord, mes demandes, nouvelle demande. */
@RestController
@RequestMapping("/api/v1/demandes-recrutement")
public class DemandeRecrutementController {

    public record DemandeResponse(Long id, Long posteId, String poste, String departement,
                                  Integer effectif, String motif, BigDecimal budget,
                                  LocalDate dateSouhaitee, String statut, LocalDateTime dateDemande,
                                  List<String> actionsDisponibles) {}

    public record DashboardResponse(long nombreDemandes, List<DemandeResponse> recentes) {}

    public record NouvelleDemandeRequest(@NotNull Long posteId, @NotNull Integer effectif, @NotBlank String motif,
                                         BigDecimal budget, LocalDate dateSouhaitee) {}
    public record ValidationRequest(@NotBlank String statut, String commentaire) {}

    private final DemandeRecrutementRepository demandeRepository;
    private final DemandeRecrutementMachine machine;
    private final AccessScope accessScope;

    public DemandeRecrutementController(DemandeRecrutementRepository demandeRepository, DemandeRecrutementMachine machine, AccessScope accessScope) {
        this.demandeRepository = demandeRepository;
        this.machine = machine;
        this.accessScope = accessScope;
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CONSULTER')")
    @GetMapping("/synthese")
    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<DemandeResponse> demandes = mesDemandes(null, null, null);
        return new DashboardResponse(demandes.size(), demandes.stream().limit(5).toList());
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CONSULTER')")
    @GetMapping
    @Transactional(readOnly = true)
    public List<DemandeResponse> mesDemandes(@RequestParam(required = false) String statut,
                                             @RequestParam(required = false) Long departementId,
                                             @RequestParam(required = false) Long managerId) {
        // TODO : filtrer sur le manager connecte (DemandeRecrutement.manager) une fois l'auth en place
        return accessScope.demandesVisibles().stream()
                .sorted(Comparator.comparing(DemandeRecrutement::getDateCreation,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .filter(d -> statut == null || statut.equals(d.getStatutValidation().name()))
                .filter(d -> departementId == null || (d.getPoste() != null && d.getPoste().getDepartement() != null
                        && departementId.equals(d.getPoste().getDepartement().getId())))
                .filter(d -> managerId == null || (d.getManager() != null && managerId.equals(d.getManager().getId())))
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @PostMapping
    public DemandeResponse creer(@Valid @RequestBody NouvelleDemandeRequest request) {
        // TODO : DemandeRecrutementService.creer(request, managerConnecte)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Creation de demande a implementer");
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CONSULTER')")
    @GetMapping("/{id}")
    public DemandeResponse detail(@PathVariable Long id) {
        return toResponse(accessScope.requireDemande(id));
    }

    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_VERIFIER_RH','PERM_DEMANDE_APPROUVER_DIR')")
    @PatchMapping("/{id}/validation")
    public DemandeResponse valider(@PathVariable Long id, @Valid @RequestBody ValidationRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Validation de demande à implémenter");
    }

    private DemandeResponse toResponse(DemandeRecrutement d) {
        var poste = d.getPoste();
        return new DemandeResponse(d.getId(),
                poste != null ? poste.getId() : null,
                poste != null ? poste.getIntitule() : null,
                poste != null && poste.getDepartement() != null ? poste.getDepartement().getLibelle() : null,
                d.getEffectifDemande(), d.getMotif(), d.getBudgetAlloue(), d.getDateSouhaitee(),
                d.getStatutValidation().name(), d.getDateCreation(), machine.actionsDisponibles(d, WorkflowContext.utilisateurCourant(null)));
    }
}
