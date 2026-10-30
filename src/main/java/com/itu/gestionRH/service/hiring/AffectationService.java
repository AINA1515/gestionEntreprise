package com.itu.gestionRH.service.hiring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.enums.ContratActe;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.enums.PosteStatut;
import com.itu.gestionRH.entity.hiring.Affectation;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.workflow.PosteMachine;
import com.itu.gestionRH.workflow.WorkflowContext;

/**
 * Mutation d'un salarié déjà en poste : il quitte son poste actuel (qui redevient vacant s'il n'a plus
 * personne) et prend un autre poste. Rien n'est écrasé : l'affectation et le contrat précédents sont clôturés,
 * une nouvelle affectation et un avenant sont créés.
 */
@Service
public class AffectationService {
    private final AffectationRepository affectations;
    private final ContratRepository contrats;
    private final EmployeRepository employes;
    private final PosteRepository postes;
    private final PosteMachine posteMachine;

    public AffectationService(AffectationRepository affectations, ContratRepository contrats,
            EmployeRepository employes, PosteRepository postes, PosteMachine posteMachine) {
        this.affectations = affectations;
        this.contrats = contrats;
        this.employes = employes;
        this.postes = postes;
        this.posteMachine = posteMachine;
    }

    @Transactional(readOnly = true)
    public List<Affectation> historique(Long employeId) {
        return affectations.findByEmployeIdOrderByDateDebutDescIdDesc(employeId);
    }

    @Transactional
    public Affectation muter(Long employeId, Long nouveauPosteId, LocalDate dateEffet, String motif,
            BigDecimal nouveauSalaire, Long superieurId, WorkflowContext contexte) {
        var employe = employes.findById(employeId).orElseThrow(() -> new NotFoundException("Employé introuvable."));
        if (employe.getStatutActivite() != EmployeStatut.ACTIF)
            throw new BusinessRuleException("EMPLOYE_NON_ACTIF", "Seul un salarié actif peut changer de poste.");
        LocalDate effet = dateEffet == null ? LocalDate.now() : dateEffet;
        if (effet.isAfter(LocalDate.now()))
            throw new BusinessRuleException("DATE_EFFET_FUTURE", "La date d'effet ne peut pas être dans le futur.");
        if (motif == null || motif.isBlank())
            throw new BusinessRuleException("MOTIF_REQUIS", "Le motif de l'affectation est obligatoire.");
        if (nouveauSalaire != null && nouveauSalaire.signum() < 0)
            throw new BusinessRuleException("SALAIRE_CONTRAT_REQUIS", "Le salaire brut doit être positif ou nul.");

        Affectation ancienne = affectations.findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(employeId)
                .orElseThrow(() -> new BusinessRuleException("AFFECTATION_COURANTE_ABSENTE", "Ce salarié n'a pas d'affectation en cours."));
        if (ancienne.getPoste().getId().equals(nouveauPosteId))
            throw new BusinessRuleException("MEME_POSTE", "Le salarié occupe déjà ce poste.");
        if (effet.isBefore(ancienne.getDateDebut()))
            throw new BusinessRuleException("DATE_EFFET_INVALIDE", "La date d'effet précède le début de l'affectation actuelle.");

        var nouveauPoste = postes.findById(nouveauPosteId).orElseThrow(() -> new NotFoundException("Poste introuvable."));
        if (nouveauPoste.getStatutPoste() != PosteStatut.VACANT && nouveauPoste.getStatutPoste() != PosteStatut.CREE)
            throw new BusinessRuleException("POSTE_NON_DISPONIBLE", "Ce poste n'est pas disponible : il doit être vacant.");

        Contrat actuel = contrats.findFirstByEmployeIdAndStatutContratInOrderByDateDebutDescIdDesc(
                employeId, List.of(ContratStatut.EN_COURS)).orElseThrow(() -> new BusinessRuleException(
                        "CONTRAT_EN_COURS_ABSENT", "Un contrat en cours est nécessaire pour changer de poste."));

        // 1. Clôture du contrat et de l'affectation actuels (les lignes restent, rien n'est supprimé).
        LocalDate finContrat = effet.minusDays(1).isBefore(actuel.getDateDebut()) ? actuel.getDateDebut() : effet.minusDays(1);
        actuel.setDateFinEffective(finContrat);
        actuel.setStatutContrat(ContratStatut.TERMINE);
        contrats.save(actuel);
        ancienne.setDateFin(effet);
        affectations.saveAndFlush(ancienne);

        // 2. Avenant sur le nouveau poste (le poste d'un contrat valide ne se modifie pas : trigger Oracle).
        Contrat avenant = Contrat.nouvelle();
        avenant.setEmploye(employe);
        avenant.setPoste(nouveauPoste);
        avenant.setTypeContrat(actuel.getTypeContrat());
        avenant.setTypeActe(ContratActe.AVENANT);
        avenant.setContratPrecedent(actuel);
        avenant.setDateDebut(effet);
        avenant.setDateFin(actuel.getDateFin());
        avenant.setSalaireBrut(nouveauSalaire != null ? nouveauSalaire : actuel.getSalaireBrut());
        avenant.setDevise(actuel.getDevise());
        avenant.setStatutContrat(ContratStatut.EN_COURS);
        contrats.saveAndFlush(avenant);

        // 3. Nouvelle affectation.
        Affectation nouvelle = Affectation.nouvelle();
        nouvelle.setEmploye(employe);
        nouvelle.setPoste(nouveauPoste);
        nouvelle.setContrat(avenant);
        nouvelle.setDateDebut(effet);
        nouvelle.setMotif(motif.trim());
        if (superieurId != null && !superieurId.equals(employeId))
            nouvelle.setSuperieur(employes.findById(superieurId).orElseThrow(() -> new NotFoundException("Supérieur introuvable.")));
        affectations.saveAndFlush(nouvelle);

        // 4. Ancien poste : vacant s'il n'a plus personne ; nouveau poste : pourvu.
        var ancienPoste = ancienne.getPoste();
        ancienPoste.getAffectations().removeIf(a -> a.getId() != null && a.getId().equals(ancienne.getId()));
        if (affectations.findByPosteIdAndDateFinIsNull(ancienPoste.getId()).isEmpty()
                && ancienPoste.getStatutPoste() == PosteStatut.POURVU)
            posteMachine.transition(ancienPoste, "liberer", contexte);
        postes.save(ancienPoste);

        if (nouveauPoste.getStatutPoste() == PosteStatut.CREE) posteMachine.transition(nouveauPoste, "ouvrir", contexte);
        nouveauPoste.getAffectations().add(nouvelle);
        posteMachine.transition(nouveauPoste, "pourvoir", contexte);
        postes.save(nouveauPoste);
        return nouvelle;
    }
}
