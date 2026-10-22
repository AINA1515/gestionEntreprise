package com.itu.gestionRH.service.notifications;

import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.hiring.PropositionEmbaucheRepository;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.repository.recruitment.OffreEmploiRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.repository.security.UtilisateurRoleRepository;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationWorkflowListener {
    private final NotificationService notifications;
    private final UtilisateurRoleRepository roles;
    private final UtilisateurRepository utilisateurs;
    private final DemandeRecrutementRepository demandes;
    private final OffreEmploiRepository offres;
    private final CandidatureRepository candidatures;
    private final PropositionEmbaucheRepository propositions;

    public NotificationWorkflowListener(NotificationService notifications, UtilisateurRoleRepository roles,
            UtilisateurRepository utilisateurs, DemandeRecrutementRepository demandes, OffreEmploiRepository offres,
            CandidatureRepository candidatures, PropositionEmbaucheRepository propositions) {
        this.notifications=notifications; this.roles=roles; this.utilisateurs=utilisateurs; this.demandes=demandes;
        this.offres=offres; this.candidatures=candidatures; this.propositions=propositions;
    }

    /** BEFORE_COMMIT conserve l'atomicité de l'outbox avec la transition métier. */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void traiter(WorkflowTransitionEvent event) {
        String code = null;
        Set<Utilisateur> destinataires = new LinkedHashSet<>();
        String entite = event.typeEntite();
        Long id = event.idEntite();

        switch (entite) {
            case "DemandeRecrutement" -> {
                var demande = demandes.findById(id).orElse(null);
                if (demande == null) return;
                switch (event.etatApres()) {
                    case "ATTENTE_RH" -> { code="DEMANDE_SOUMISE"; destinataires.addAll(parRole("CHARGE_RECRUTEMENT")); }
                    case "ATTENTE_DIRECTION" -> { code="DEMANDE_TRANSMISE_DIRECTION"; destinataires.addAll(parRole("DIRECTION")); }
                    case "VALIDEE" -> { code="DEMANDE_VALIDEE"; destinataires.add(demande.getManager()); }
                    case "REFUSEE" -> { code="DEMANDE_REFUSEE"; destinataires.add(demande.getManager()); }
                    default -> { return; }
                }
            }
            case "OffreEmploi" -> {
                if (!"PUBLIEE".equals(event.etatApres())) return;
                var offre = offres.findById(id).orElse(null);
                if (offre == null) return;
                code="OFFRE_PUBLIEE";
                if (offre.getDemande() != null) destinataires.add(offre.getDemande().getManager());
            }
            case "Candidature" -> {
                var candidature = candidatures.findById(id).orElse(null);
                if (candidature == null) return;
                switch (event.etatApres()) {
                    case "PRESELECTIONNEE" -> {
                        code="CANDIDATURE_PRESELECTIONNEE";
                        if (candidature.getOffre() != null && candidature.getOffre().getDemande() != null) destinataires.add(candidature.getOffre().getDemande().getManager());
                    }
                    case "EN_ENTRETIEN" -> {
                        code="ENTRETIEN_PLANIFIE";
                        candidature.getCandidat();
                        utilisateurs.findByCandidatId(candidature.getCandidat().getId()).ifPresent(destinataires::add);
                        if (candidature.getOffre() != null && candidature.getOffre().getDemande() != null) destinataires.add(candidature.getOffre().getDemande().getManager());
                    }
                    case "REJETEE" -> {
                        code="CANDIDATURE_REJETEE";
                        utilisateurs.findByCandidatId(candidature.getCandidat().getId()).ifPresent(destinataires::add);
                    }
                    default -> { return; }
                }
            }
            case "PropositionEmbauche" -> {
                var proposition = propositions.findById(id).orElse(null);
                if (proposition == null) return;
                switch (event.etatApres()) {
                    case "ENVOYEE" -> {
                        code="PROPOSITION_ENVOYEE";
                        var candidature=proposition.getCandidature();
                        if (candidature != null && candidature.getCandidat() != null) utilisateurs.findByCandidatId(candidature.getCandidat().getId()).ifPresent(destinataires::add);
                    }
                    case "ACCEPTEE" -> { code="PROPOSITION_ACCEPTEE"; destinataires.addAll(parRole("CHARGE_RECRUTEMENT")); }
                    case "REFUSEE" -> { code="PROPOSITION_REFUSEE"; destinataires.addAll(parRole("CHARGE_RECRUTEMENT")); }
                    default -> { return; }
                }
            }
            default -> { return; }
        }
        if (code != null && !destinataires.isEmpty()) {
            notifications.programmer(code, destinataires,
                    Map.of("reference", entite + " #" + id, "message", event.etatAvant() + " → " + event.etatApres()), entite, id);
        }
    }

    private Set<Utilisateur> parRole(String code) { return new LinkedHashSet<>(roles.findActiveUsersByRoleCode(code, LocalDate.now())); }
}
