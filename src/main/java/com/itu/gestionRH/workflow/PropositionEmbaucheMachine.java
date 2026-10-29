package com.itu.gestionRH.workflow;

import com.itu.gestionRH.entity.enums.PropositionStatut;
import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PropositionEmbaucheMachine {
    private final StateMachine<PropositionEmbauche, PropositionStatut> machine;
    public PropositionEmbaucheMachine(ApplicationEventPublisher publisher) {
        var b = StateMachine.<PropositionEmbauche, PropositionStatut>builder("PropositionEmbauche",
                PropositionEmbauche::getStatutProposition, PropositionEmbauche::appliquerTransitionWorkflow,
                PropositionEmbauche::getId, publisher);
        b.transition(PropositionStatut.BROUILLON, "envoyer", PropositionStatut.ENVOYEE,
                java.util.Set.of(), java.util.Set.of("PROPOSITION_EMETTRE"), r -> true, r -> {},
                "WORKFLOW_PRECONDITION_REFUSEE", "L’envoi de cette proposition n’est pas autorisé.");
        b.transition(PropositionStatut.ENVOYEE, "accepter", PropositionStatut.ACCEPTEE, "PROPOSITION_REPONDRE");
        b.transition(PropositionStatut.ENVOYEE, "refuser", PropositionStatut.REFUSEE,
                Set.of(), Set.of("PROPOSITION_REPONDRE"), r -> WorkflowRules.justificationPresente(r.contexte()), r -> {},
                "JUSTIFICATION_OBLIGATOIRE", "Un motif de refus est obligatoire.");
        b.transition(PropositionStatut.ENVOYEE, "expirer", PropositionStatut.EXPIREE, "PROPOSITION_EMETTRE");
        for (PropositionStatut source : List.of(PropositionStatut.BROUILLON, PropositionStatut.ENVOYEE))
            b.transition(source, "annuler", PropositionStatut.ANNULEE, "PROPOSITION_EMETTRE");
        machine = b.build();
    }
    public WorkflowTransitionEvent transition(PropositionEmbauche entity, String action, WorkflowContext context) { return machine.transition(entity, action, context); }
    public List<String> actionsDisponibles(PropositionEmbauche entity, WorkflowContext user) { return machine.actionsDisponibles(entity, user); }
}
