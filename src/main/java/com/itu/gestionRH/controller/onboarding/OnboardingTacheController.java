package com.itu.gestionRH.controller.onboarding;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/onboarding/taches")
public class OnboardingTacheController {
    public record TacheResponse(Long id, String intitule, String statut, String serviceResponsable) {}
    public record MiseAJourTacheRequest(String statut, String commentaire) {}
    @GetMapping("/mes-taches")
    public List<TacheResponse> mesTaches() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Consultation des tâches à implémenter");
    }
    @PatchMapping("/{id}")
    public TacheResponse mettreAJour(@PathVariable Long id, @RequestBody MiseAJourTacheRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Mise à jour de tâche à implémenter");
    }
}
