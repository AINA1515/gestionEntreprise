package com.itu.gestionRH.workflow;

import com.itu.gestionRH.entity.enums.OnboardingStatut;
import com.itu.gestionRH.entity.enums.TacheStatut;
import com.itu.gestionRH.entity.onboarding.Onboarding;
import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OnboardingMachine {
    private final StateMachine<Onboarding, OnboardingStatut> machine;
    public OnboardingMachine(ApplicationEventPublisher publisher) {
        var b = StateMachine.<Onboarding, OnboardingStatut>builder("Onboarding", Onboarding::getStatutGlobal,
                Onboarding::appliquerTransitionWorkflow, Onboarding::getId, publisher);
        for (OnboardingStatut source : OnboardingStatut.values()) {
            for (OnboardingStatut target : OnboardingStatut.values()) {
                if (source == target) continue;
                b.transition(source, "recalculer", target, java.util.Set.of(), java.util.Set.of(),
                        r -> derive(r.entite()) == target, r -> {}, "ONBOARDING_STATUT_NON_DERIVE",
                        "Le statut global ne correspond pas à l'état des tâches obligatoires.");
            }
        }
        machine = b.build();
    }

    public WorkflowTransitionEvent recalculer(Onboarding onboarding, WorkflowContext context) {
        OnboardingStatut cible = derive(onboarding);
        if (cible == onboarding.getStatutGlobal()) return null;
        return machine.transition(onboarding, "recalculer", context);
    }

    public List<String> actionsDisponibles(Onboarding onboarding, WorkflowContext user) {
        return derive(onboarding) == onboarding.getStatutGlobal() ? List.of() : List.of("recalculer");
    }

    public static OnboardingStatut derive(Onboarding onboarding) {
        List<OnboardingTache> taches = onboarding.getOnboardingTaches();
        if (taches.isEmpty() || taches.stream().noneMatch(t -> t.getStatutTache() != TacheStatut.A_FAIRE))
            return OnboardingStatut.NON_DEBUTE;
        boolean obligatoiresTerminees = taches.stream().filter(t -> Boolean.TRUE.equals(t.getEstObligatoire()))
                .allMatch(t -> t.getStatutTache() == TacheStatut.VALIDEE);
        return obligatoiresTerminees ? OnboardingStatut.COMPLETE : OnboardingStatut.EN_COURS;
    }
}
