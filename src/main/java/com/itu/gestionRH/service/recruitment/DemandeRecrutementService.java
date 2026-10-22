package com.itu.gestionRH.service.recruitment;

import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.workflow.DemandeRecrutementMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandeRecrutementService {
    private final DemandeRecrutementMachine machine;

    public DemandeRecrutementService(DemandeRecrutementMachine machine) { this.machine = machine; }

    @Transactional
    public WorkflowTransitionEvent transition(DemandeRecrutement entite, String action, WorkflowContext contexte) {
        return machine.transition(entite, action, contexte);
    }
}
