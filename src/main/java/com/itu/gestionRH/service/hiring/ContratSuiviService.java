package com.itu.gestionRH.service.hiring;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.enums.PosteStatut;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.workflow.PosteMachine;
import com.itu.gestionRH.workflow.WorkflowContext;

/** Cycle de vie des contrats après leur création : activation, fin, sortie, alertes d'échéance. */
@Service
public class ContratSuiviService {
    private static final List<ContratStatut> ACTIFS = List.of(ContratStatut.EN_ATTENTE, ContratStatut.EN_COURS);

    private final ContratRepository contrats;
    private final AffectationRepository affectations;
    private final EmployeRepository employes;
    private final PosteRepository postes;
    private final PosteMachine posteMachine;

    public ContratSuiviService(ContratRepository contrats, AffectationRepository affectations,
            EmployeRepository employes, PosteRepository postes, PosteMachine posteMachine) {
        this.contrats = contrats;
        this.affectations = affectations;
        this.employes = employes;
        this.postes = postes;
        this.posteMachine = posteMachine;
    }

    @Transactional(readOnly = true)
    public Contrat obtenir(Long id) {
        return contrats.findById(id).orElseThrow(() -> new NotFoundException("Contrat introuvable."));
    }

    /** CDD en cours dont la fin tombe dans les {@code jours} prochains jours, du plus proche au plus lointain. */
    @Transactional(readOnly = true)
    public List<Contrat> expirantBientot(int jours) {
        int fenetre = Math.max(1, Math.min(jours, 365));
        LocalDate aujourdHui = LocalDate.now();
        return contrats.findByTypeContratAndStatutContratInAndDateFinBetweenOrderByDateFinAsc(
                ContratType.CDD, List.of(ContratStatut.EN_COURS), aujourdHui, aujourdHui.plusDays(fenetre));
    }

    /**
     * Clôture un contrat sans écraser l'historique : la ligne reste, datée de fin effective et passée à TERMINE.
     * Si le salarié n'a plus aucun contrat actif, il est marqué SORTI et son poste libéré.
     */
    @Transactional
    public Contrat terminer(Long id, LocalDate dateFin, WorkflowContext contexte) {
        Contrat contrat = obtenir(id);
        if (contrat.getStatutContrat() != ContratStatut.EN_COURS && contrat.getStatutContrat() != ContratStatut.EN_ATTENTE)
            throw new BusinessRuleException("CONTRAT_NON_ACTIF", "Seul un contrat en cours ou en attente peut être terminé.");
        LocalDate fin = dateFin == null ? LocalDate.now() : dateFin;
        if (fin.isBefore(contrat.getDateDebut()))
            throw new BusinessRuleException("DATES_CONTRAT_INVALIDES", "La date de fin ne peut pas précéder le début du contrat.");
        contrat.setDateFinEffective(fin);
        contrat.setStatutContrat(ContratStatut.TERMINE);
        contrats.saveAndFlush(contrat);

        Long employeId = contrat.getEmploye().getId();
        var poste = contrat.getPoste();
        affectations.findFirstByEmployeIdAndPosteIdAndDateFinIsNullOrderByDateDebutDesc(employeId, poste.getId()).ifPresent(a -> {
            a.setDateFin(fin.isBefore(a.getDateDebut()) ? a.getDateDebut() : fin);
            affectations.saveAndFlush(a);
        });

        boolean autreContratActif = contrats.findByEmployeIdAndStatutContratIn(employeId, ACTIFS).stream()
                .anyMatch(c -> !c.getId().equals(contrat.getId()));
        if (!autreContratActif) {
            var employe = contrat.getEmploye();
            employe.setStatutActivite(EmployeStatut.SORTI);
            employes.save(employe);
        }
        boolean posteEncoreOccupe = !affectations.findByPosteIdAndDateFinIsNull(poste.getId()).isEmpty();
        if (!posteEncoreOccupe && poste.getStatutPoste() == PosteStatut.POURVU) {
            posteMachine.transition(poste, "liberer", contexte);
            postes.save(poste);
        }
        return contrat;
    }

    /** Tâche quotidienne : active les contrats qui démarrent et termine les CDD échus. */
    @Transactional
    public int appliquerEcheances() {
        LocalDate aujourdHui = LocalDate.now();
        int traites = 0;
        for (Contrat c : contrats.findByStatutContratAndDateDebutLessThanEqual(ContratStatut.EN_ATTENTE, aujourdHui)) {
            c.setStatutContrat(ContratStatut.EN_COURS);
            contrats.save(c);
            traites++;
        }
        var contexte = WorkflowContext.of("SYSTEME", Set.of(), Set.of("CONTRAT_GERER", "UTILISATEUR_ADMIN"),
                "Fin automatique de CDD arrivé à échéance");
        for (Contrat c : contrats.findByTypeContratAndStatutContratAndDateFinBefore(ContratType.CDD, ContratStatut.EN_COURS, aujourdHui)) {
            terminer(c.getId(), c.getDateFin(), contexte);
            traites++;
        }
        return traites;
    }
}
