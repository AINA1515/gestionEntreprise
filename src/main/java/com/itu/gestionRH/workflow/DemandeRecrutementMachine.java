package com.itu.gestionRH.workflow;

import com.itu.gestionRH.entity.enums.DemandeStatut;
import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DemandeRecrutementMachine {
    private final StateMachine<DemandeRecrutement, DemandeStatut> machine;
    public DemandeRecrutementMachine(ApplicationEventPublisher publisher) {
        var b = StateMachine.<DemandeRecrutement, DemandeStatut>builder("DemandeRecrutement",
                DemandeRecrutement::getStatutValidation, DemandeRecrutement::appliquerTransitionWorkflow,
                DemandeRecrutement::getId, publisher);
        b.transition(DemandeStatut.BROUILLON, "soumettre", DemandeStatut.ATTENTE_RH, "DEMANDE_CREER");
        b.transition(DemandeStatut.ATTENTE_RH, "transmettre_direction", DemandeStatut.ATTENTE_DIRECTION, "DEMANDE_VERIFIER_RH");
        b.transition(DemandeStatut.ATTENTE_RH, "refuser", DemandeStatut.REFUSEE,
                Set.of(), Set.of("DEMANDE_VERIFIER_RH"), r -> WorkflowRules.justificationPresente(r.contexte()), r -> {},
                "JUSTIFICATION_OBLIGATOIRE", "Une justification est obligatoire pour refuser la demande.");
        b.transition(DemandeStatut.ATTENTE_RH, "renvoyer_manager", DemandeStatut.BROUILLON, "DEMANDE_VERIFIER_RH");
        b.transition(DemandeStatut.ATTENTE_DIRECTION, "valider", DemandeStatut.VALIDEE, "DEMANDE_APPROUVER_DIR");
        b.transition(DemandeStatut.ATTENTE_DIRECTION, "refuser", DemandeStatut.REFUSEE,
                Set.of(), Set.of("DEMANDE_APPROUVER_DIR"), r -> WorkflowRules.justificationPresente(r.contexte()), r -> {},
                "JUSTIFICATION_OBLIGATOIRE", "Une justification est obligatoire pour refuser la demande.");
        machine = b.build();
    }
    public WorkflowTransitionEvent transition(DemandeRecrutement entity, String action, WorkflowContext context) { return machine.transition(entity, action, context); }
    public List<String> actionsDisponibles(DemandeRecrutement entity, WorkflowContext user) { return machine.actionsDisponibles(entity, user); }
}
