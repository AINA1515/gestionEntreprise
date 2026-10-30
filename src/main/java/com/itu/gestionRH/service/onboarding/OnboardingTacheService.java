package com.itu.gestionRH.service.onboarding;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.DocumentStatut;
import com.itu.gestionRH.entity.enums.TypeDocument;
import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import com.itu.gestionRH.repository.documents.DocumentRepository;
import com.itu.gestionRH.workflow.OnboardingTacheMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;

@Service
public class OnboardingTacheService {
    /** Tâches dont la validation exige une pièce déposée en GED, par code du modèle de tâche. */
    private static final Map<String, TypeDocument> JUSTIFICATIFS = Map.of(
            "SAL_RIB", TypeDocument.RIB,
            "SAL_IDENTITE", TypeDocument.PIECE_IDENTITE,
            "SAL_VITALE", TypeDocument.CARTE_VITALE,
            "SAL_REGLEMENT", TypeDocument.REGLEMENT_SIGNE,
            "RH_CONTRAT_SIGNE", TypeDocument.CONTRAT_SIGNE);

    private final OnboardingTacheMachine machine;
    private final DocumentRepository documents;

    public OnboardingTacheService(OnboardingTacheMachine machine, DocumentRepository documents) {
        this.machine = machine;
        this.documents = documents;
    }

    /** Type de pièce exigé pour valider la tâche, ou {@code null} si aucune pièce n'est requise. */
    public static TypeDocument justificatifRequis(OnboardingTache tache) {
        if (tache.getModele() == null || tache.getModele().getCode() == null) return null;
        return JUSTIFICATIFS.get(tache.getModele().getCode());
    }

    @Transactional
    public WorkflowTransitionEvent transition(OnboardingTache entite, String action, WorkflowContext contexte) {
        if ("valider".equals(action)) verifierJustificatif(entite);
        return machine.transition(entite, action, contexte);
    }

    private void verifierJustificatif(OnboardingTache tache) {
        TypeDocument requis = justificatifRequis(tache);
        if (requis == null) return;
        boolean present = documents.findFirstByEntiteRattachementAndEntiteIdAndTypeDocumentAndStatutDocumentOrderByNumeroVersionDesc(
                DocumentEntite.ONBOARDING_TACHE, tache.getId(), requis, DocumentStatut.ACTIF).isPresent();
        if (!present)
            throw new BusinessRuleException("JUSTIFICATIF_REQUIS",
                    "Déposez d'abord le document demandé (" + requis.name().replace('_', ' ').toLowerCase() + ") avant de valider cette tâche.",
                    org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
