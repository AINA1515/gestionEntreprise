package com.itu.gestionRH.service.security;

import com.itu.gestionRH.common.AuthException;
import com.itu.gestionRH.entity.enums.CompteStatut;
import com.itu.gestionRH.entity.enums.CompteType;
import com.itu.gestionRH.entity.enums.LogConnexionResultat;
import com.itu.gestionRH.entity.security.*;
import com.itu.gestionRH.repository.security.*;
import com.itu.gestionRH.security.jwt.JwtService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final int MAX_ECHECS = 5;
    private static final Pattern MOT_DE_PASSE = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{12,72}$");
    private final UtilisateurRepository utilisateurs;
    private final CandidatRepository candidats;
    private final RoleRhRepository roles;
    private final UtilisateurRoleRepository utilisateurRoles;
    private final RolePermissionRepository rolePermissions;
    private final LogConnexionRepository logs;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurs, CandidatRepository candidats, RoleRhRepository roles,
                       UtilisateurRoleRepository utilisateurRoles, RolePermissionRepository rolePermissions,
                       LogConnexionRepository logs, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utilisateurs = utilisateurs; this.candidats = candidats; this.roles = roles;
        this.utilisateurRoles = utilisateurRoles; this.rolePermissions = rolePermissions; this.logs = logs;
        this.passwordEncoder = passwordEncoder; this.jwtService = jwtService;
    }

    @Transactional(noRollbackFor = AuthException.class)
    public LoginResult authentifier(String identifiant, String motDePasse, String adresseIp, String userAgent) {
        String ident = identifiant == null ? "" : identifiant.trim();
        Utilisateur user = utilisateurs.findByLoginIgnoreCaseOrEmailIgnoreCase(ident, ident).orElse(null);
        if (user == null) {
            enregistrer(null, ident, LogConnexionResultat.ECHEC, adresseIp, userAgent);
            throw identifiantsIncorrects();
        }
        if (user.getStatutCompte() == CompteStatut.VERROUILLE) {
            enregistrer(user, ident, LogConnexionResultat.VERROUILLAGE, adresseIp, userAgent);
            throw new AuthException("ACCOUNT_LOCKED", "Compte verrouillé après cinq tentatives. Contactez l'administrateur.", HttpStatus.LOCKED);
        }
        if (user.getStatutCompte() != CompteStatut.ACTIF) {
            enregistrer(user, ident, LogConnexionResultat.ECHEC, adresseIp, userAgent);
            throw identifiantsIncorrects();
        }
        if (!motDePasseCorrect(user.getMotDePasseHash(), motDePasse)) {
            int echecs = (user.getNbEchecsConnexion() == null ? 0 : user.getNbEchecsConnexion()) + 1;
            user.setNbEchecsConnexion(echecs);
            boolean verrouille = echecs >= MAX_ECHECS;
            if (verrouille) user.setStatutCompte(CompteStatut.VERROUILLE);
            utilisateurs.save(user);
            enregistrer(user, ident, verrouille ? LogConnexionResultat.VERROUILLAGE : LogConnexionResultat.ECHEC, adresseIp, userAgent);
            if (verrouille) throw new AuthException("ACCOUNT_LOCKED", "Compte verrouillé après cinq tentatives. Contactez l'administrateur.", HttpStatus.LOCKED);
            throw identifiantsIncorrects();
        }
        Set<String> roleCodes = rolesActifs(user).stream().map(RoleRh::getCode).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissions = permissions(roleCodes);
        user.setNbEchecsConnexion(0);
        user.setDateDerniereConnexion(LocalDateTime.now());
        utilisateurs.save(user);
        enregistrer(user, ident, LogConnexionResultat.SUCCES, adresseIp, userAgent);
        UserView view = view(user, roleCodes, permissions);
        if (user.getDateExpirationMdp() != null && user.getDateExpirationMdp().isBefore(LocalDate.now()))
            throw new AuthException("PASSWORD_EXPIRED", "Votre mot de passe a expiré. Veuillez le modifier.", HttpStatus.FORBIDDEN);
        JwtService.IssuedToken jwt = jwtService.issue(user.getId(), user.getLogin(), List.copyOf(roleCodes), List.copyOf(permissions));
        return new LoginResult(jwt.value(), jwt.expiresAt(), view);
    }

    @Transactional(readOnly = true)
    public UserView utilisateurCourant(Long id) {
        Utilisateur user = utilisateurs.findById(id).orElseThrow(() -> new AuthException("INVALID_TOKEN", "Jeton invalide.", HttpStatus.UNAUTHORIZED));
        if (user.getStatutCompte() != CompteStatut.ACTIF)
            throw new AuthException("ACCOUNT_INACTIVE", "Ce compte n'est pas actif.", HttpStatus.UNAUTHORIZED);
        Set<String> roleCodes = rolesActifs(user).stream().map(RoleRh::getCode).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return view(user, roleCodes, permissions(roleCodes));
    }

    @Transactional(readOnly = true)
    public LoginResult renouvelerJeton(Long id) {
        Utilisateur user = utilisateurs.findById(id).orElseThrow(() -> new AuthException("INVALID_TOKEN", "Jeton invalide.", HttpStatus.UNAUTHORIZED));
        if (user.getStatutCompte() != CompteStatut.ACTIF)
            throw new AuthException("ACCOUNT_INACTIVE", "Ce compte n'est pas actif.", HttpStatus.UNAUTHORIZED);
        Set<String> roleCodes = rolesActifs(user).stream().map(RoleRh::getCode).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissions = permissions(roleCodes);
        UserView view = view(user, roleCodes, permissions);
        JwtService.IssuedToken jwt = jwtService.issue(user.getId(), user.getLogin(), List.copyOf(roleCodes), List.copyOf(permissions));
        return new LoginResult(jwt.value(), jwt.expiresAt(), view);
    }

    @Transactional
    public void inscrireCandidat(Inscription inscription) {
        verifierComplexite(inscription.motDePasse());
        String email = inscription.email().trim().toLowerCase(Locale.ROOT);
        if (utilisateurs.existsByEmailIgnoreCase(email) || candidats.existsByEmailIgnoreCase(email))
            throw new AuthException("EMAIL_ALREADY_USED", "Cette adresse courriel est déjà utilisée.", HttpStatus.CONFLICT);
        RoleRh role = roles.findByCode("CANDIDAT");
        if (role == null || !Boolean.TRUE.equals(role.getEstActif()))
            throw new IllegalStateException("Le rôle CANDIDAT doit être présent et actif dans le référentiel.");
        Candidat candidat = Candidat.nouvelle();
        candidat.setNom(inscription.nom().trim()); candidat.setPrenom(inscription.prenom().trim());
        candidat.setEmail(email); candidat.setTelephone(blankToNull(inscription.telephone()));
        candidat.setConsentementRgpd(true); candidat.setDateConsentement(LocalDate.now());
        candidats.saveAndFlush(candidat);
        Utilisateur user = Utilisateur.nouvelle();
        user.setLogin("CAND-" + candidat.getId()); user.setEmail(email); user.setNomAffichage((inscription.prenom().trim() + " " + inscription.nom().trim()).trim());
        user.setMotDePasseHash(passwordEncoder.encode(inscription.motDePasse())); user.setTypeCompte(CompteType.CANDIDAT);
        user.setStatutCompte(CompteStatut.ACTIF); user.setNbEchecsConnexion(0); user.setCandidat(candidat);
        utilisateurs.saveAndFlush(user);
        UtilisateurRole attribution = UtilisateurRole.nouvelle(); attribution.setUtilisateur(user); attribution.setRole(role);
        attribution.setDateDebut(LocalDate.now()); utilisateurRoles.save(attribution);
    }

    @Transactional
    public void changerMotDePasse(Long userId, String ancien, String nouveau) {
        verifierComplexite(nouveau);
        Utilisateur user = utilisateurs.findById(userId).orElseThrow(() -> identifiantsIncorrects());
        if (!motDePasseCorrect(user.getMotDePasseHash(), ancien)) throw identifiantsIncorrects();
        user.setMotDePasseHash(passwordEncoder.encode(nouveau)); user.setDateExpirationMdp(null); user.setNbEchecsConnexion(0);
        utilisateurs.save(user);
    }

    private List<RoleRh> rolesActifs(Utilisateur user) {
        return utilisateurRoles.findByUtilisateurIdAndDateFinIsNullAndDateDebutLessThanEqual(user.getId(), LocalDate.now())
                .stream().map(UtilisateurRole::getRole).filter(role -> Boolean.TRUE.equals(role.getEstActif())).toList();
    }
    private Set<String> permissions(Set<String> roleCodes) {
        Set<String> result = new LinkedHashSet<>();
        rolePermissions.findAll().stream().filter(rp -> roleCodes.contains(rp.getRole().getCode()))
                .map(RolePermission::getPermission).map(Permission::getCode).forEach(result::add);
        return result;
    }
    private UserView view(Utilisateur user, Set<String> roleCodes, Set<String> permissions) {
        return new UserView(user.getId(), user.getLogin(), user.getEmail(), user.getNomAffichage(),
                user.getTypeCompte().name(), List.copyOf(roleCodes), List.copyOf(permissions));
    }
    private void enregistrer(Utilisateur user, String identifiant, LogConnexionResultat resultat, String ip, String ua) {
        LogConnexion log = LogConnexion.nouvelle();
        log.renseigner(user, identifiant == null ? null : identifiant.substring(0, Math.min(100, identifiant.length())),
                resultat, LocalDateTime.now(), ip == null ? null : ip.substring(0, Math.min(45, ip.length())),
                ua == null ? null : ua.substring(0, Math.min(300, ua.length())));
        logs.save(log);
    }
    private boolean motDePasseCorrect(String hash, String password) {
        if (hash == null || password == null || !hash.startsWith("$2")) return false;
        try { return passwordEncoder.matches(password, hash); } catch (IllegalArgumentException ex) { return false; }
    }
    private AuthException identifiantsIncorrects() { return new AuthException("INVALID_CREDENTIALS", "Identifiants incorrects.", HttpStatus.UNAUTHORIZED); }
    private void verifierComplexite(String password) {
        if (password == null || !MOT_DE_PASSE.matcher(password).matches()
                || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new AuthException("PASSWORD_POLICY", "Le mot de passe doit contenir 12 à 72 caractères, avec une minuscule, une majuscule, un chiffre et un caractère spécial.", HttpStatus.BAD_REQUEST);
    }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record UserView(Long id, String login, String email, String nomAffichage, String typeCompte,
                           List<String> roles, List<String> permissions) {}
    public record LoginResult(String token, java.time.Instant expiresAt, UserView utilisateur) {}
    public record Inscription(String nom, String prenom, String email, String telephone, String motDePasse) {}
}
