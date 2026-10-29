package com.itu.gestionRH.controller.security;

import com.itu.gestionRH.security.jwt.JwtAuthenticationFilter.JwtPrincipal;
import com.itu.gestionRH.service.security.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class UtilisateurController {
    private final AuthService authService;
    public UtilisateurController(AuthService authService) { this.authService = authService; }

    public record LoginRequest(@NotBlank String identifiant, @NotBlank String motDePasse) {}
    public record LoginResponse(String token, java.time.Instant expiresAt, AuthService.UserView utilisateur) {}
    public record InscriptionRequest(@NotBlank @Size(max = 100) String nom,
                                     @NotBlank @Size(max = 100) String prenom,
                                     @NotBlank @Email @Size(max = 150) String email,
                                     @Size(max = 30) String telephone,
                                     @NotBlank @Size(min = 12, max = 72) String motDePasse,
                                     @AssertTrue boolean consentementRgpd) {}
    public record ChangerMotDePasseRequest(@NotBlank String ancienMotDePasse, @NotBlank String nouveauMotDePasse) {}

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        var result = authService.authentifier(request.identifiant(), request.motDePasse(),
                servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent"));
        return new LoginResponse(result.token(), result.expiresAt(), result.utilisateur());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public AuthService.UserView me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return authService.utilisateurCourant(principal.userId());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/refresh")
    public LoginResponse refresh() {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var result = authService.renouvelerJeton(principal.userId());
        return new LoginResponse(result.token(), result.expiresAt(), result.utilisateur());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/inscription-candidat")
    public ResponseEntity<Void> inscription(@Valid @RequestBody InscriptionRequest request) {
        authService.inscrireCandidat(new AuthService.Inscription(request.nom(), request.prenom(), request.email(),
                request.telephone(), request.motDePasse()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/changer-mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(@Valid @RequestBody ChangerMotDePasseRequest request) {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.changerMotDePasse(principal.userId(), request.ancienMotDePasse(), request.nouveauMotDePasse());
        return ResponseEntity.noContent().build();
    }
}
