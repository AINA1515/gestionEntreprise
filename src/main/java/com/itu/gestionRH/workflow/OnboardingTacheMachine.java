package com.itu.gestionRH.workflow;

import com.itu.gestionRH.entity.enums.TacheStatut;
import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OnboardingTacheMachine {
    private final StateMachine<OnboardingTache, TacheStatut> machine;
    public OnboardingTacheMachine(ApplicationEventPublisher publisher, OnboardingMachine onboardingMachine) {
        var b = StateMachine.<OnboardingTache, TacheStatut>builder("OnboardingTache", OnboardingTache::getStatutTache,
                OnboardingTache::appliquerTransitionWorkflow, OnboardingTache::getId, publisher);
        b.transition(TacheStatut.A_FAIRE, "commencer", TacheStatut.EN_COURS, Set.of(), Set.of("TACHE_TRAITER"),
                r -> estAssignee(r.entite(), r.contexte()), r -> {},
                "TACHE_SERVICE_INTERDIT", "Cette tâche est attribuée à un autre service.");
        for (TacheStatut source : List.of(TacheStatut.A_FAIRE, TacheStatut.EN_COURS)) {
            b.transition(source, "valider", TacheStatut.VALIDEE,
                    Set.of(), Set.of("TACHE_TRAITER"),
                    r -> estAssignee(r.entite(), r.contexte()),
                    r -> onboardingMachine.recalculer(r.entite().getOnboarding(), r.contexte()),
                    "TACHE_SERVICE_INTERDIT", "Cette tâche est attribuée à un autre service.");
        }
        machine = b.build();
    }
    private static boolean estAssignee(OnboardingTache task, WorkflowContext context) {
        return task.getAssigneeAId()!=null && task.getAssigneeAId().equals(context.utilisateurId());
    }
    public WorkflowTransitionEvent transition(OnboardingTache entity, String action, WorkflowContext context) { return machine.transition(entity, action, context); }
    public List<String> actionsDisponibles(OnboardingTache entity, WorkflowContext user) { return machine.actionsDisponibles(entity, user); }
}
