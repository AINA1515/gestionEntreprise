package com.itu.gestionRH.workflow;

import com.itu.gestionRH.entity.enums.CandidatureStatut;
import com.itu.gestionRH.entity.enums.EvaluationDecision;
import com.itu.gestionRH.entity.recruitment.Candidature;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CandidatureMachine {
    private final StateMachine<Candidature, CandidatureStatut> machine;
    public CandidatureMachine(ApplicationEventPublisher publisher, EmployeRepository employes) {
        var b = StateMachine.<Candidature, CandidatureStatut>builder("Candidature",
                Candidature::getStatutCandidature, Candidature::appliquerTransitionWorkflow, Candidature::getId, publisher);
        b.transition(CandidatureStatut.RECUE, "preselectionner", CandidatureStatut.PRESELECTIONNEE, "CANDIDATURE_PRESELECTIONNER");
        b.transition(CandidatureStatut.PRESELECTIONNEE, "planifier_entretien", CandidatureStatut.EN_ENTRETIEN, "ENTRETIEN_PLANIFIER");
        b.transition(CandidatureStatut.EN_ENTRETIEN, "retenir", CandidatureStatut.RETENUE,
                Set.of(), Set.of("EVALUATION_SAISIR"), r -> r.entite().getEntretiens().stream()
                        .anyMatch(e -> e.getEvaluation() != null && e.getEvaluation().getDecision() == EvaluationDecision.FAVORABLE),
                r -> {}, "EVALUATION_NON_FAVORABLE", "Une évaluation favorable est requise pour retenir la candidature.");
        for (CandidatureStatut source : List.of(CandidatureStatut.RECUE, CandidatureStatut.PRESELECTIONNEE,
                CandidatureStatut.EN_ENTRETIEN, CandidatureStatut.RETENUE)) {
            b.transition(source, "rejeter", CandidatureStatut.REJETEE, Set.of(), Set.of("EVALUATION_SAISIR", "CANDIDATURE_PRESELECTIONNER"),
                    r -> WorkflowRules.justificationPresente(r.contexte())
                            && !(source == CandidatureStatut.RETENUE && r.entite().getCandidat() != null
                            && employes.existsByCandidatId(r.entite().getCandidat().getId())),
                    r -> {}, "JUSTIFICATION_OU_EMBAUCHE_INTERDITE",
                    "Une justification est obligatoire; une candidature déjà embauchée ne peut être rejetée.");
        }
        machine = b.build();
    }
    public WorkflowTransitionEvent transition(Candidature entity, String action, WorkflowContext context) { return machine.transition(entity, action, context); }
    public List<String> actionsDisponibles(Candidature entity, WorkflowContext user) { return machine.actionsDisponibles(entity, user); }
}
