package com.itu.gestionRH.workflow;

import com.itu.gestionRH.entity.enums.PosteStatut;
import com.itu.gestionRH.entity.organization.Poste;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PosteMachine {
    private final StateMachine<Poste, PosteStatut> machine;
    public PosteMachine(ApplicationEventPublisher publisher) {
        var b = StateMachine.<Poste, PosteStatut>builder("Poste", Poste::getStatutPoste,
                Poste::appliquerTransitionWorkflow, Poste::getId, publisher);
        b.transition(PosteStatut.CREE, "ouvrir", PosteStatut.VACANT, "GESTIONNAIRE_RH");
        b.transition(PosteStatut.VACANT, "pourvoir", PosteStatut.POURVU, Set.of("GESTIONNAIRE_RH"), Set.of(),
                r -> r.entite().getAffectations().stream().anyMatch(a -> a.getDateFin() == null && a.getEmploye() != null),
                r -> {}, "AFFECTATION_ACTIVE_REQUISE", "Le poste ne peut être pourvu sans affectation à un employé.");
        b.transition(PosteStatut.POURVU, "liberer", PosteStatut.VACANT, "GESTIONNAIRE_RH");
        for (PosteStatut source : List.of(PosteStatut.CREE, PosteStatut.VACANT))
            b.transition(source, "fermer", PosteStatut.FERME, "GESTIONNAIRE_RH");
        machine = b.build();
    }
    public WorkflowTransitionEvent transition(Poste entity, String action, WorkflowContext context) { return machine.transition(entity, action, context); }
    public List<String> actionsDisponibles(Poste entity, WorkflowContext user) { return machine.actionsDisponibles(entity, user); }
}
