package com.itu.gestionRH.workflow;

import com.itu.gestionRH.entity.enums.DemandeStatut;
import com.itu.gestionRH.entity.enums.OffreStatut;
import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OffreEmploiMachine {
    private final StateMachine<OffreEmploi, OffreStatut> machine;
    public OffreEmploiMachine(ApplicationEventPublisher publisher) {
        var b = StateMachine.<OffreEmploi, OffreStatut>builder("OffreEmploi",
                OffreEmploi::getStatutOffre, OffreEmploi::appliquerTransitionWorkflow, OffreEmploi::getId, publisher);
        b.transition(OffreStatut.BROUILLON, "publier", OffreStatut.PUBLIEE,
                java.util.Set.of(), java.util.Set.of("OFFRE_PUBLIER"),
                r -> r.entite().getDemande() != null && r.entite().getDemande().getStatutValidation() == DemandeStatut.VALIDEE,
                r -> {}, "DEMANDE_NON_VALIDEE", "L'offre ne peut être publiée que si la demande liée est validée.");
        b.transition(OffreStatut.PUBLIEE, "cloturer", OffreStatut.CLOTUREE, "DEMANDE_VERIFIER_RH");
        machine = b.build();
    }
    public WorkflowTransitionEvent transition(OffreEmploi entity, String action, WorkflowContext context) { return machine.transition(entity, action, context); }
    public List<String> actionsDisponibles(OffreEmploi entity, WorkflowContext user) { return machine.actionsDisponibles(entity, user); }
}
