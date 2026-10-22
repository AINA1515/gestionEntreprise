package com.itu.gestionRH.controller.hiring;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/** Confirmation de recrutement (ex-page /rh/confirmation). */
@RestController
@RequestMapping("/api/v1/embauche")
public class PropositionEmbaucheController {

    /** typeContrat : CDD | CDI | STAGE | ALTERNANCE (contrainte CK_PROP_TYPE). */
    public record ConfirmationRequest(Long candidatureId, String typeContrat,
                                      LocalDate dateDebut, String message) {}

    public record ConfirmationResponse(Long id, Long candidatureId, String statut) {}
    public record ReponseRequest(String statut, String commentaire) {}
    public record EmployeConversionResponse(Long employeId, Long candidatId, String matricule, String statut) {}

    @PostMapping("/propositions")
    public ConfirmationResponse confirmer(@RequestBody ConfirmationRequest request) {
        // TODO : PropositionEmbaucheService.confirmer(request) (+ envoi d'e-mail)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Confirmation de recrutement a implementer");
    }

    @PatchMapping("/propositions/{id}/reponse")
    public ConfirmationResponse repondre(@PathVariable Long id, @RequestBody ReponseRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Réponse à la proposition à implémenter");
    }

    @org.springframework.web.bind.annotation.PostMapping("/candidats/{id}/convertir-employe")
    public EmployeConversionResponse convertir(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Conversion candidat vers employé à implémenter");
    }
}
