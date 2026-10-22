package com.itu.gestionRH.controller.hiring;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.repository.hiring.PropositionEmbaucheRepository;
import com.itu.gestionRH.security.AccessScope;
import org.springframework.transaction.annotation.Transactional;

/** Confirmation de recrutement (ex-page /rh/confirmation). */
@RestController
@RequestMapping("/api/v1/embauche")
public class PropositionEmbaucheController {
    private final PropositionEmbaucheRepository propositions;
    private final AccessScope accessScope;
    public PropositionEmbaucheController(PropositionEmbaucheRepository propositions, AccessScope accessScope) {
        this.propositions=propositions; this.accessScope=accessScope;
    }

    /** typeContrat : CDD | CDI | STAGE | ALTERNANCE (contrainte CK_PROP_TYPE). */
    public record ConfirmationRequest(Long candidatureId, String typeContrat,
                                      LocalDate dateDebut, String message) {}

    public record ConfirmationResponse(Long id, Long candidatureId, String statut, List<String> actionsDisponibles) {}
    public record ReponseRequest(String statut, String commentaire) {}
    public record EmployeConversionResponse(Long employeId, Long candidatId, String matricule, String statut) {}

    @PreAuthorize("hasAuthority('PERM_PROPOSITION_EMETTRE')")
    @PostMapping("/propositions")
    public ConfirmationResponse confirmer(@RequestBody ConfirmationRequest request) {
        // TODO : PropositionEmbaucheService.confirmer(request) (+ envoi d'e-mail)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Confirmation de recrutement a implementer");
    }

    @PreAuthorize("hasAuthority('PERM_PROPOSITION_REPONDRE')")
    @PatchMapping("/propositions/{id}/reponse")
    @Transactional(readOnly = true)
    public ConfirmationResponse repondre(@PathVariable Long id, @RequestBody ReponseRequest request) {
        var proposition=propositions.findById(id).orElseThrow(()->new NotFoundException("Proposition introuvable."));
        accessScope.requirePropositionOwner(proposition);
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Réponse à la proposition à implémenter");
    }

    @PreAuthorize("hasAuthority('PERM_EMPLOYE_CONVERTIR')")
    @org.springframework.web.bind.annotation.PostMapping("/candidats/{id}/convertir-employe")
    public EmployeConversionResponse convertir(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Conversion candidat vers employé à implémenter");
    }
}
