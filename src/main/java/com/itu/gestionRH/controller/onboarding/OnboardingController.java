package com.itu.gestionRH.controller.onboarding;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {
    public record InitialisationResponse(Long onboardingId, Long employeId, String statut) {}
    @PostMapping("/employes/{id}/initialiser-onboarding")
    public InitialisationResponse initialiser(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Initialisation de l'onboarding à implémenter");
    }
}
