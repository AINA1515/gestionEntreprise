package com.itu.gestionRH.service.recruitment;

import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import com.itu.gestionRH.workflow.workflow.OffreEmploiMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OffreEmploiService {
    private final OffreEmploiMachine machine;

    public OffreEmploiService(OffreEmploiMachine machine) { this.machine = machine; }

    @Transactional
    public WorkflowTransitionEvent transition(OffreEmploi entite, String action, WorkflowContext contexte) {
        return machine.transition(entite, action, contexte);
    }
}
