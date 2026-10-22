package com.itu.gestionRH.service.onboarding;

import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import com.itu.gestionRH.workflow.OnboardingTacheMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingTacheService {
    private final OnboardingTacheMachine machine;

    public OnboardingTacheService(OnboardingTacheMachine machine) { this.machine = machine; }

    @Transactional
    public WorkflowTransitionEvent transition(OnboardingTache entite, String action, WorkflowContext contexte) {
        return machine.transition(entite, action, contexte);
    }
}
