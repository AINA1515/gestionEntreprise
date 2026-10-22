package com.itu.gestionRH.controller.recruitment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Decision de recrutement du RH (ex-page /rh/decision). */
@RestController
@RequestMapping("/api/v1/recrutement")
public class EvaluationCandidatController {

    /** decision : FAVORABLE | DEFAVORABLE | RESERVE (contrainte CK_EVAL_DEC). */
    public record DecisionRequest(Long candidatureId, String decision, String observations) {}

    public record DecisionResponse(Long id, Long candidatureId, String decision) {}

    @PostMapping("/candidatures/{id}/evaluations")
    public DecisionResponse enregistrer(@org.springframework.web.bind.annotation.PathVariable Long id,
                                         @RequestBody DecisionRequest request) {
        // TODO : EvaluationCandidatService.enregistrerDecision(request)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Enregistrement de la decision a implementer");
    }
}
