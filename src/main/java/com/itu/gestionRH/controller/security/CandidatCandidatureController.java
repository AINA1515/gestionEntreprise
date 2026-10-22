package com.itu.gestionRH.controller.security;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/candidat/candidatures")
public class CandidatCandidatureController {
    public record CandidatureResponse(Long id, Long offreId, String poste, String statut, String datePostulation) {}
    @GetMapping
    public List<CandidatureResponse> lister() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Suivi des candidatures à implémenter");
    }
}
