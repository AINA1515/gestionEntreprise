package com.itu.gestionRH.service.organization;

import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.workflow.workflow.PosteMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PosteService {
    private final PosteMachine machine;

    public PosteService(PosteMachine machine) { this.machine = machine; }

    @Transactional
    public WorkflowTransitionEvent transition(Poste entite, String action, WorkflowContext contexte) {
        return machine.transition(entite, action, contexte);
    }
}
