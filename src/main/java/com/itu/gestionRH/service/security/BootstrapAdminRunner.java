package com.itu.gestionRH.service.security;

import com.itu.gestionRH.entity.enums.CompteStatut;
import com.itu.gestionRH.entity.enums.CompteType;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.entity.security.UtilisateurRole;
import com.itu.gestionRH.repository.security.RoleRhRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.repository.security.UtilisateurRoleRepository;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapAdminRunner implements CommandLineRunner {
    private final boolean enabled;
    private final String login;
    private final String email;
    private final String password;
    private final UtilisateurRepository utilisateurs;
    private final RoleRhRepository roles;
    private final UtilisateurRoleRepository attributions;
    private final PasswordEncoder encoder;

    public BootstrapAdminRunner(@Value("${app.bootstrap-admin.enabled:false}") boolean enabled,
            @Value("${app.bootstrap-admin.login:}") String login,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String password,
            UtilisateurRepository utilisateurs, RoleRhRepository roles,
            UtilisateurRoleRepository attributions, PasswordEncoder encoder) {
        this.enabled = enabled; this.login = login; this.email = email; this.password = password;
        this.utilisateurs = utilisateurs; this.roles = roles; this.attributions = attributions; this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) return;
        var adminRole = roles.findByCode("ADMIN");
        if (adminRole == null) throw new IllegalStateException("Le référentiel ROLE_RH ne contient pas ADMIN.");
        boolean adminExists = utilisateurs.findAll().stream().anyMatch(user -> attributions
                .findByUtilisateurIdAndDateFinIsNullAndDateDebutLessThanEqual(user.getId(), LocalDate.now()).stream()
                .anyMatch(ur -> "ADMIN".equals(ur.getRole().getCode())));
        if (adminExists) return;
        if (login.isBlank() || email.isBlank() || password.length() < 12)
            throw new IllegalStateException("Définir BOOTSTRAP_ADMIN_LOGIN, BOOTSTRAP_ADMIN_EMAIL et un BOOTSTRAP_ADMIN_PASSWORD d'au moins 12 caractères.");
        if (utilisateurs.existsByEmailIgnoreCase(email)) throw new IllegalStateException("L'adresse du compte administrateur de démarrage est déjà utilisée.");
        Utilisateur admin = Utilisateur.nouvelle();
        admin.setLogin(login); admin.setEmail(email); admin.setNomAffichage("Administrateur");
        admin.setMotDePasseHash(encoder.encode(password)); admin.setTypeCompte(CompteType.INTERNE);
        admin.setStatutCompte(CompteStatut.ACTIF); admin.setNbEchecsConnexion(0);
        utilisateurs.saveAndFlush(admin);
        UtilisateurRole attribution = UtilisateurRole.nouvelle(); attribution.setUtilisateur(admin);
        attribution.setRole(adminRole); attribution.setDateDebut(LocalDate.now()); attributions.save(attribution);
    }
}
