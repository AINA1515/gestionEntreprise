package com.itu.gestionRH.service.notifications;

import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.documents.DocumentObligatoireRepository;
import com.itu.gestionRH.repository.documents.DocumentRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.repository.security.UtilisateurRoleRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alertes d'expiration des pièces du dossier (sur le modèle de l'alerte de fin de CDD) : un message par salarié et par jour,
 * adressé à la RH et au salarié, pour les pièces obligatoires qui expirent dans leur délai d'alerte ou ont expiré depuis moins de 30 jours.
 */
@Service
public class DocumentExpirationReminderService {
    private static final int JOURS_APRES_EXPIRATION = 30;

    private final DocumentObligatoireRepository obligatoires;
    private final DocumentRepository documents;
    private final EmployeRepository employes;
    private final UtilisateurRepository utilisateurs;
    private final UtilisateurRoleRepository roles;
    private final NotificationService notifications;

    public DocumentExpirationReminderService(DocumentObligatoireRepository obligatoires, DocumentRepository documents,
            EmployeRepository employes, UtilisateurRepository utilisateurs, UtilisateurRoleRepository roles,
            NotificationService notifications) {
        this.obligatoires = obligatoires; this.documents = documents; this.employes = employes;
        this.utilisateurs = utilisateurs; this.roles = roles; this.notifications = notifications;
    }

    @Transactional
    public void programmerAlertes() {
        LocalDate aujourdHui = LocalDate.now();
        Map<Long, List<String>> parSalarie = new LinkedHashMap<>();
        for (var o : obligatoires.findByEstActifTrueOrderByOrdreAscIdAsc()) {
            for (var d : documents.expirantEntre(o.getTypeDocument(), aujourdHui.minusDays(JOURS_APRES_EXPIRATION),
                    aujourdHui.plusDays(o.getDelaiAlerteJours()))) {
                String ligne = o.getLibelle() + (d.getDateExpiration().isBefore(aujourdHui) ? " : expirée le " : " : expire le ") + d.getDateExpiration();
                List<String> lignes = parSalarie.computeIfAbsent(d.getEntiteId(), k -> new java.util.ArrayList<>());
                if (!lignes.contains(ligne)) lignes.add(ligne);
            }
        }
        if (parSalarie.isEmpty()) return;
        var rh = roles.findActiveUsersByRoleCode("GESTIONNAIRE_RH", aujourdHui);
        parSalarie.forEach((employeId, lignes) -> {
            var employe = employes.findById(employeId).orElse(null);
            if (employe == null) return;
            Set<Utilisateur> destinataires = new LinkedHashSet<>(rh);
            utilisateurs.findByEmployeId(employeId).ifPresent(destinataires::add);
            notifications.programmerUneFoisParJour("DOCUMENT_EXPIRATION_PROCHE", destinataires,
                    Map.of("reference", employe.getPrenom() + " " + employe.getNom() + " (" + employe.getMatricule() + ")",
                            "message", String.join(" ; ", lignes)),
                    "EMPLOYE", employeId);
        });
    }
}
