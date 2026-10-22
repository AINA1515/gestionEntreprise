package com.itu.gestionRH.service.hiring;

import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import com.itu.gestionRH.workflow.PropositionEmbaucheMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropositionEmbaucheService {
    private final PropositionEmbaucheMachine machine;

    public PropositionEmbaucheService(PropositionEmbaucheMachine machine) { this.machine = machine; }

    @Transactional
    public WorkflowTransitionEvent transition(PropositionEmbauche entite, String action, WorkflowContext contexte) {
        return machine.transition(entite, action, contexte);
    }
}
