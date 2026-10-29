package com.itu.gestionRH.service.onboarding;

import com.itu.gestionRH.entity.onboarding.Onboarding;
import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import com.itu.gestionRH.entity.enums.OnboardingStatut;
import com.itu.gestionRH.entity.enums.TacheStatut;
import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.repository.onboarding.ModeleOnbTacheRepository;
import com.itu.gestionRH.repository.onboarding.OnboardingRepository;
import com.itu.gestionRH.repository.onboarding.OnboardingTacheRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.workflow.OnboardingMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {
    private final OnboardingMachine machine;
    private final OnboardingRepository onboardings;
    private final OnboardingTacheRepository taches;
    private final ModeleOnbTacheRepository modeles;
    private final EmployeRepository employes;
    public OnboardingService(OnboardingMachine machine, OnboardingRepository onboardings,
            OnboardingTacheRepository taches, ModeleOnbTacheRepository modeles, EmployeRepository employes) {
        this.machine = machine; this.onboardings = onboardings; this.taches = taches;
        this.modeles = modeles; this.employes = employes;
    }

    @Transactional
    public Onboarding initialiser(Long employeId) {
        Employe employe = employes.findById(employeId).orElseThrow(() -> new com.itu.gestionRH.common.NotFoundException("Employé introuvable."));
        Onboarding existant = onboardings.findByEmployeId(employeId);
        if (existant != null && !taches.findByOnboardingEmployeIdOrderByDateLimiteAscIdAsc(employeId).isEmpty()) return existant;
        java.time.LocalDate debut = employe.getDateEmbaucheInitiale() == null ? java.time.LocalDate.now() : employe.getDateEmbaucheInitiale();
        Onboarding onboarding = existant;
        if (onboarding == null) {
            onboarding = Onboarding.nouvelle();
            onboarding.setEmploye(employe);
            onboarding.setDateDebut(debut);
            onboarding.setDateFinPrevue(debut.plusDays(30));
            onboarding.appliquerTransitionWorkflow(OnboardingStatut.NON_DEBUTE);
            onboardings.saveAndFlush(onboarding);
        }
        var modelesActifs = modeles.findByEstActifTrueOrderByOrdreAsc();
        if (modelesActifs.isEmpty()) throw new com.itu.gestionRH.common.BusinessRuleException("MODELES_ONBOARDING_ABSENTS", "Aucun modèle d'onboarding actif n'est configuré.");
        for (var modele : modelesActifs) {
            OnboardingTache tache = OnboardingTache.nouvelle();
            tache.setOnboarding(onboarding);
            tache.setModele(modele);
            tache.setIntituleTache(modele.getIntitule());
            tache.setDescription(modele.getDescription());
            tache.setServiceResponsable(modele.getServiceResponsable());
            tache.setEstObligatoire(modele.getEstObligatoire());
            tache.appliquerTransitionWorkflow(TacheStatut.A_FAIRE);
            tache.setDateLimite(debut.plusDays(modele.getDelaiJours()));
            onboarding.getOnboardingTaches().add(tache);
            taches.save(tache);
        }
        taches.flush();
        return onboarding;
    }


    @Transactional(readOnly = true)
    public Onboarding obtenir(Long employeId) {
        Onboarding onboarding = onboardings.findByEmployeId(employeId);
        if (onboarding == null) throw new com.itu.gestionRH.common.NotFoundException("Onboarding introuvable.");
        onboarding.getOnboardingTaches().size();
        return onboarding;
    }

    @Transactional
    public WorkflowTransitionEvent recalculerStatut(Onboarding onboarding, WorkflowContext contexte) {
        return machine.recalculer(onboarding, contexte);
    }
}
