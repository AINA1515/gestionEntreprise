package com.itu.gestionRH.controller.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Authentification (API REST). Remplace les anciennes pages "/" et "/login".
 * La logique (verification du mot de passe, session/JWT) reste a brancher
 * dans UtilisateurService : voir TODO.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class UtilisateurController {

    public record LoginRequest(String identifiant, String motDePasse) {}

    public record UtilisateurResponse(Long id, String login, String email,
                                      String nomAffichage, String typeCompte, String role) {}

    public record LoginResponse(String token, UtilisateurResponse utilisateur) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // TODO : UtilisateurService.authentifier(identifiant, motDePasse) -> token + utilisateur
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Authentification a implementer");
    }

    @GetMapping("/me")
    public UtilisateurResponse me() {
        // TODO : utilisateur courant a partir du token / de la session
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Utilisateur courant a implementer");
    }

    @PostMapping("/logout")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // TODO : invalider le token / la session
    }
}
