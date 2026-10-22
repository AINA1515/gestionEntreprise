package com.itu.gestionRH.service.notifications;

import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.enums.PropositionStatut;
import com.itu.gestionRH.entity.enums.TacheStatut;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.hiring.PropositionEmbaucheRepository;
import com.itu.gestionRH.repository.onboarding.OnboardingTacheRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.repository.security.UtilisateurRoleRepository;
import com.itu.gestionRH.workflow.PropositionEmbaucheMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationReminderService {
    private final OnboardingTacheRepository taches;
    private final PropositionEmbaucheRepository propositions;
    private final ContratRepository contrats;
    private final UtilisateurRepository utilisateurs;
    private final UtilisateurRoleRepository roles;
    private final NotificationService notifications;
    private final PropositionEmbaucheMachine propositionMachine;

    public NotificationReminderService(OnboardingTacheRepository taches, PropositionEmbaucheRepository propositions,
            ContratRepository contrats, UtilisateurRepository utilisateurs, UtilisateurRoleRepository roles,
            NotificationService notifications, PropositionEmbaucheMachine propositionMachine) {
        this.taches=taches; this.propositions=propositions; this.contrats=contrats; this.utilisateurs=utilisateurs;
        this.roles=roles; this.notifications=notifications; this.propositionMachine=propositionMachine;
    }

    @Transactional
    public void programmerRappels() {
        LocalDate aujourdHui=LocalDate.now();
        for (var tache : taches.findByDateLimiteBeforeAndStatutTacheIn(aujourdHui, List.of(TacheStatut.A_FAIRE, TacheStatut.EN_COURS))) {
            if (tache.getAssigneeAId() == null) continue;
            utilisateurs.findById(tache.getAssigneeAId()).ifPresent(user -> notifications.programmerUneFoisParJour(
                    "ONBOARDING_TACHE_EN_RETARD", List.of(user), Map.of("reference", "Tâche #" + tache.getId(),
                            "message", tache.getIntituleTache()), "ONBOARDING_TACHE", tache.getId()));
        }

        for (var proposition : propositions.findByStatutPropositionAndDateLimiteReponseBefore(PropositionStatut.ENVOYEE, aujourdHui)) {
            propositionMachine.transition(proposition, "expirer", WorkflowContext.of("SYSTEME", Set.of(), Set.of("PROPOSITION_EMETTRE"), "Expiration automatique"));
        }

        LocalDate dans30Jours=aujourdHui.plusDays(30);
        var rh = roles.findActiveUsersByRoleCode("GESTIONNAIRE_RH", aujourdHui);
        for (var contrat : contrats.findByTypeContratAndStatutContratAndDateFinBetween(ContratType.CDD, ContratStatut.EN_COURS, aujourdHui, dans30Jours)) {
            Set<com.itu.gestionRH.entity.security.Utilisateur> destinataires=new LinkedHashSet<>(rh);
            if (contrat.getEmploye() != null) utilisateurs.findByEmployeId(contrat.getEmploye().getId()).ifPresent(destinataires::add);
            notifications.programmerUneFoisParJour("CONTRAT_FIN_PROCHE", destinataires,
                    Map.of("reference", "Contrat #" + contrat.getId(), "date", contrat.getDateFin().toString(),
                            "message", "Veuillez préparer le renouvellement ou la fin du contrat."), "CONTRAT", contrat.getId());
        }
    }
}
