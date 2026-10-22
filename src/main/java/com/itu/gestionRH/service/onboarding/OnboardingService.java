package com.itu.gestionRH.service.onboarding;

import com.itu.gestionRH.entity.onboarding.Onboarding;
import com.itu.gestionRH.workflow.OnboardingMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {
    private final OnboardingMachine machine;
    public OnboardingService(OnboardingMachine machine) { this.machine = machine; }

    @Transactional
    public WorkflowTransitionEvent recalculerStatut(Onboarding onboarding, WorkflowContext contexte) {
        return machine.recalculer(onboarding, contexte);
    }
}
