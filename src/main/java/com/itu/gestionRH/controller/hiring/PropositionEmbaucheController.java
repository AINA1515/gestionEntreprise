package com.itu.gestionRH.controller.hiring;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Confirmation de recrutement (ex-page /rh/confirmation). */
@RestController
@RequestMapping("/api/rh/confirmations")
public class PropositionEmbaucheController {

    /** typeContrat : CDD | CDI | STAGE | ALTERNANCE (contrainte CK_PROP_TYPE). */
    public record ConfirmationRequest(Long candidatureId, String typeContrat,
                                      LocalDate dateDebut, String message) {}

    public record ConfirmationResponse(Long id, Long candidatureId, String statut) {}

    @PostMapping
    public ConfirmationResponse confirmer(@RequestBody ConfirmationRequest request) {
        // TODO : PropositionEmbaucheService.confirmer(request) (+ envoi d'e-mail)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Confirmation de recrutement a implementer");
    }
}
