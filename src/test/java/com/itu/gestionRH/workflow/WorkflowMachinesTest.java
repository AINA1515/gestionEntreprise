package com.itu.gestionRH.workflow;

import static org.junit.jupiter.api.Assertions.*;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.entity.enums.*;
import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import com.itu.gestionRH.entity.onboarding.*;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.entity.recruitment.*;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class WorkflowMachinesTest {
    private final List<Object> events = new ArrayList<>();
    private ApplicationEventPublisher publisher;
    private EmployeRepository employes;

    @BeforeEach void setup() {
        events.clear();
        publisher = events::add;
        employes = (EmployeRepository) Proxy.newProxyInstance(EmployeRepository.class.getClassLoader(),
                new Class<?>[] { EmployeRepository.class }, (proxy, method, args) -> {
                    if (method.getName().equals("existsByCandidatId")) return false;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == long.class) return 0L;
                    return null;
                });
    }

    private WorkflowContext role(String role) {
        Set<String> permissions=switch(role) {
            case "MANAGER" -> Set.of("DEMANDE_CREER","EVALUATION_SAISIR");
            case "DIRECTION" -> Set.of("DEMANDE_APPROUVER_DIR");
            case "GESTIONNAIRE_RH" -> Set.of("DEMANDE_VERIFIER_RH","OFFRE_PUBLIER","CANDIDATURE_PRESELECTIONNER","ENTRETIEN_PLANIFIER","PROPOSITION_EMETTRE","EMPLOYE_CONVERTIR","CONTRAT_GERER","ONBOARDING_ADMIN");
            case "CANDIDAT" -> Set.of("PROPOSITION_REPONDRE","CANDIDATURE_POSTULER");
            case "IT","MOYENS_GENERAUX","SALARIE" -> Set.of("TACHE_TRAITER");
            default -> Set.of();
        };
        return WorkflowContext.ofUtilisateur(7L,"test", Set.of(role), permissions, null);
    }
    private WorkflowContext comment(String role) { var c=role(role); return WorkflowContext.of("test",c.roles(),c.permissions(),"motif valable"); }

    @Test void demandeSoumettrePublieEvenement() {
        var e = DemandeRecrutement.nouvelle(); e.appliquerTransitionWorkflow(DemandeStatut.BROUILLON);
        var event = new DemandeRecrutementMachine(publisher).transition(e, "soumettre", role("MANAGER"));
        assertEquals(DemandeStatut.ATTENTE_RH, e.getStatutValidation()); assertEquals("BROUILLON", event.etatAvant()); assertEquals(1, events.size());
    }
    @Test void demandeTransitionInterdite() {
        var e = DemandeRecrutement.nouvelle(); e.appliquerTransitionWorkflow(DemandeStatut.BROUILLON);
        assertThrows(BusinessRuleException.class, () -> new DemandeRecrutementMachine(publisher).transition(e, "valider", role("DIRECTION")));
    }
    @Test void demandePermissionManquante() {
        var e = DemandeRecrutement.nouvelle(); e.appliquerTransitionWorkflow(DemandeStatut.BROUILLON);
        assertThrows(BusinessRuleException.class, () -> new DemandeRecrutementMachine(publisher).transition(e, "soumettre", role("CANDIDAT")));
    }
    @Test void demandeRefusExigeJustification() {
        var e = DemandeRecrutement.nouvelle(); e.appliquerTransitionWorkflow(DemandeStatut.ATTENTE_RH);
        assertThrows(BusinessRuleException.class, () -> new DemandeRecrutementMachine(publisher).transition(e, "refuser", role("GESTIONNAIRE_RH")));
    }

    @Test void offrePublierSeulementAvecDemandeValidee() {
        var e = OffreEmploi.nouvelle(); e.appliquerTransitionWorkflow(OffreStatut.BROUILLON);
        var demande = DemandeRecrutement.nouvelle(); demande.appliquerTransitionWorkflow(DemandeStatut.VALIDEE); e.setDemande(demande);
        new OffreEmploiMachine(publisher).transition(e, "publier", role("GESTIONNAIRE_RH"));
        assertEquals(OffreStatut.PUBLIEE, e.getStatutOffre());
    }
    @Test void offreTransitionInterditeDepuisBrouillon() {
        var e = OffreEmploi.nouvelle(); e.appliquerTransitionWorkflow(OffreStatut.BROUILLON);
        assertThrows(BusinessRuleException.class, () -> new OffreEmploiMachine(publisher).transition(e, "cloturer", role("GESTIONNAIRE_RH")));
    }

    @Test void candidaturePreselection() {
        var e = Candidature.nouvelle(); e.appliquerTransitionWorkflow(CandidatureStatut.RECUE);
        new CandidatureMachine(publisher, employes).transition(e, "preselectionner", role("GESTIONNAIRE_RH"));
        assertEquals(CandidatureStatut.PRESELECTIONNEE, e.getStatutCandidature());
    }
    @Test void candidatureTransitionInterdite() {
        var e = Candidature.nouvelle(); e.appliquerTransitionWorkflow(CandidatureStatut.RECUE);
        assertThrows(BusinessRuleException.class, () -> new CandidatureMachine(publisher, employes).transition(e, "retenir", role("MANAGER")));
    }
    @Test void candidaturePermissionManquante() {
        var e = Candidature.nouvelle(); e.appliquerTransitionWorkflow(CandidatureStatut.RECUE);
        assertThrows(BusinessRuleException.class, () -> new CandidatureMachine(publisher, employes).transition(e, "preselectionner", role("CANDIDAT")));
    }
    @Test void candidatureRejetExigeMotif() {
        var e = Candidature.nouvelle(); e.appliquerTransitionWorkflow(CandidatureStatut.RECUE);
        assertThrows(BusinessRuleException.class, () -> new CandidatureMachine(publisher, employes).transition(e, "rejeter", role("GESTIONNAIRE_RH")));
    }

    @Test void propositionEnvoyer() {
        var e = PropositionEmbauche.nouvelle(); e.appliquerTransitionWorkflow(PropositionStatut.BROUILLON);
        new PropositionEmbaucheMachine(publisher).transition(e, "envoyer", role("GESTIONNAIRE_RH"));
        assertEquals(PropositionStatut.ENVOYEE, e.getStatutProposition());
    }
    @Test void propositionRefusExigeMotif() {
        var e = PropositionEmbauche.nouvelle(); e.appliquerTransitionWorkflow(PropositionStatut.ENVOYEE);
        assertThrows(BusinessRuleException.class, () -> new PropositionEmbaucheMachine(publisher).transition(e, "refuser", role("CANDIDAT")));
    }
    @Test void propositionPermissionManquante() {
        var e = PropositionEmbauche.nouvelle(); e.appliquerTransitionWorkflow(PropositionStatut.BROUILLON);
        assertThrows(BusinessRuleException.class, () -> new PropositionEmbaucheMachine(publisher).transition(e, "envoyer", role("MANAGER")));
    }

    @Test void posteOuvrirLeRendVacant() {
        var e = Poste.nouvelle(); e.appliquerTransitionWorkflow(PosteStatut.CREE);
        new PosteMachine(publisher).transition(e, "ouvrir", role("GESTIONNAIRE_RH"));
        assertEquals(PosteStatut.VACANT, e.getStatutPoste());
    }
    @Test void posteTransitionInterdite() {
        var e = Poste.nouvelle(); e.appliquerTransitionWorkflow(PosteStatut.CREE);
        assertThrows(BusinessRuleException.class, () -> new PosteMachine(publisher).transition(e, "pourvoir", role("GESTIONNAIRE_RH")));
    }

    @Test void onboardingStatutDeriveDesTaches() {
        var e = Onboarding.nouvelle(); e.appliquerTransitionWorkflow(OnboardingStatut.NON_DEBUTE);
        var t = OnboardingTache.nouvelle(); t.appliquerTransitionWorkflow(TacheStatut.EN_COURS); t.setEstObligatoire(true);
        e.setOnboardingTaches(List.of(t));
        assertEquals(OnboardingStatut.EN_COURS, OnboardingMachine.derive(e));
    }
    @Test void tacheDemarrePourLeServiceResponsable() {
        var e = OnboardingTache.nouvelle(); e.appliquerTransitionWorkflow(TacheStatut.A_FAIRE); e.setServiceResponsable(ServiceResponsable.IT); e.setAssigneeAId(7L);
        new OnboardingTacheMachine(publisher, new OnboardingMachine(publisher)).transition(e, "commencer", role("IT"));
        assertEquals(TacheStatut.EN_COURS, e.getStatutTache());
    }
    @Test void tacheRefuseAutreService() {
        var e = OnboardingTache.nouvelle(); e.appliquerTransitionWorkflow(TacheStatut.A_FAIRE); e.setServiceResponsable(ServiceResponsable.IT); e.setAssigneeAId(8L);
        assertThrows(BusinessRuleException.class, () -> new OnboardingTacheMachine(publisher, new OnboardingMachine(publisher)).transition(e, "commencer", role("MANAGER")));
    }
}
