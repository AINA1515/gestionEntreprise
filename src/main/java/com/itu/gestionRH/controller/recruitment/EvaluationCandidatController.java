package com.itu.gestionRH.controller.recruitment;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import com.itu.gestionRH.security.AccessScope;

/** Decision de recrutement du RH (ex-page /rh/decision). */
@RestController
@RequestMapping("/api/v1/recrutement")
public class EvaluationCandidatController {
    private final AccessScope accessScope;
    public EvaluationCandidatController(AccessScope accessScope) { this.accessScope=accessScope; }

    /** decision : FAVORABLE | DEFAVORABLE | RESERVE (contrainte CK_EVAL_DEC). */
    public record DecisionRequest(Long candidatureId, String decision, String observations) {}

    public record DecisionResponse(Long id, Long candidatureId, String decision) {}

    @PreAuthorize("hasAuthority('PERM_EVALUATION_SAISIR')")
    @PostMapping("/candidatures/{id}/evaluations")
    public DecisionResponse enregistrer(@org.springframework.web.bind.annotation.PathVariable Long id,
                                         @RequestBody DecisionRequest request) {
        accessScope.requireCandidature(id);
        // TODO : EvaluationCandidatService.enregistrerDecision(request)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Enregistrement de la decision a implementer");
    }
}
