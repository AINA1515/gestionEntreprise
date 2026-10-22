package com.itu.gestionRH.service.recruitment;

import com.itu.gestionRH.entity.recruitment.Candidature;
import com.itu.gestionRH.workflow.workflow.CandidatureMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidatureService {
    private final CandidatureMachine machine;

    public CandidatureService(CandidatureMachine machine) { this.machine = machine; }

    @Transactional
    public WorkflowTransitionEvent transition(Candidature entite, String action, WorkflowContext contexte) {
        return machine.transition(entite, action, contexte);
    }
}
