package com.itu.gestionRH.service.hiring;

import java.time.LocalDate;
import java.util.Set;
import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.entity.enums.ContratActe;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.hiring.Affectation;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.enums.PosteStatut;
import com.itu.gestionRH.entity.enums.PropositionStatut;
import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.entity.security.UtilisateurRole;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.repository.security.RoleRhRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.repository.security.UtilisateurRoleRepository;
import com.itu.gestionRH.workflow.PosteMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Finalise atomicamente une proposition acceptee en dossier employe, contrat et affectation. */
@Service
public class ContratService {
    private final EmployeRepository employes;
    private final ContratRepository contrats;
    private final AffectationRepository affectations;
    private final PosteRepository postes;
    private final PosteMachine posteMachine;
    private final EntityManager entityManager;
    private final UtilisateurRepository utilisateurs;
    private final RoleRhRepository roles;
    private final UtilisateurRoleRepository attributions;

    public ContratService(EmployeRepository employes, ContratRepository contrats, AffectationRepository affectations,
            PosteRepository postes, PosteMachine posteMachine, EntityManager entityManager,
            UtilisateurRepository utilisateurs, RoleRhRepository roles, UtilisateurRoleRepository attributions) {
        this.employes = employes;
        this.contrats = contrats;
        this.affectations = affectations;
        this.postes = postes;
        this.posteMachine = posteMachine;
        this.entityManager = entityManager;
        this.utilisateurs = utilisateurs;
        this.roles = roles;
        this.attributions = attributions;
    }

    @Transactional
    public Employe finaliserEmbauche(PropositionEmbauche proposition) {
        if (proposition.getStatutProposition() != PropositionStatut.ACCEPTEE)
            throw new BusinessRuleException("PROPOSITION_NON_ACCEPTEE", "La proposition doit être acceptée avant la création du dossier salarié.");
        if (proposition.getEmploye() != null) return proposition.getEmploye();

        var candidature = proposition.getCandidature();
        var candidat = candidature == null ? null : candidature.getCandidat();
        var offre = candidature == null ? null : candidature.getOffre();
        var demande = offre == null ? null : offre.getDemande();
        Poste poste = demande == null ? null : demande.getPoste();
        if (candidat == null || poste == null)
            throw new BusinessRuleException("DONNEES_EMBAUCHE_INCOMPLETES", "Le candidat ou le poste lié à la demande est introuvable.");
        if (employes.existsByCandidatId(candidat.getId()))
            throw new BusinessRuleException("CANDIDAT_DEJA_SALARIE", "Un dossier salarié existe déjà pour ce candidat.");

        LocalDate debut = proposition.getDateDebutSouhaitee() == null ? LocalDate.now() : proposition.getDateDebutSouhaitee();
        ContratType type = proposition.getTypeContratPropose();
        LocalDate fin = proposition.getDateFinSouhaitee();
        if (type == ContratType.CDI && fin != null)
            throw new BusinessRuleException("DATE_FIN_CDI_INVALIDE", "Un contrat CDI ne doit pas avoir de date de fin.");
        if (type != ContratType.CDI && (fin == null || fin.isBefore(debut)))
            throw new BusinessRuleException("DATE_FIN_CONTRAT_REQUISE", "Une date de fin valide est requise pour ce type de contrat.");
        var salaire = proposition.getSalaireBrutPropose() != null ? proposition.getSalaireBrutPropose()
                : candidature.getSalairePropose() != null ? candidature.getSalairePropose()
                : demande.getBudgetAlloue();
        if (salaire == null || salaire.signum() < 0)
            throw new BusinessRuleException("SALAIRE_CONTRAT_REQUIS", "Le salaire du contrat doit être renseigné.");

        Employe employe = Employe.nouvelle();
        employe.setCandidat(candidat);
        employe.setNom(candidat.getNom());
        employe.setPrenom(candidat.getPrenom());
        // L'email personnel du candidat n'est pas toujours libre dans le registre
        // des emails professionnels (UX_EMP_EMAIL). Il reste conservé sur CANDIDAT.
        String email = candidat.getEmail();
        employe.setEmailProfessionnel(email != null && !email.isBlank()
                && !employes.existsByEmailProfessionnelIgnoreCase(email) ? email : null);
        employe.setTelephone(candidat.getTelephone());
        employe.setDateEmbaucheInitiale(debut);
        employe.setStatutActivite(EmployeStatut.EN_ONBOARDING);
        employes.saveAndFlush(employe);
        // Le trigger Oracle génère EMP-AAAA-NNNN à l'insertion.
        entityManager.refresh(employe);
        utilisateurs.findByCandidatId(candidat.getId()).ifPresent(utilisateur -> {
            utilisateur.setEmploye(employe);
            var roleSalarie = roles.findByCode("SALARIE");
            // Un compte candidat peut déjà avoir une attribution SALARIE planifiée
            // à la date de début du contrat. Vérifier uniquement les rôles actifs
            // aujourd'hui provoquait alors une violation de UK_UR à l'embauche future.
            boolean attributionSalarieExistante = roleSalarie != null && attributions
                    .findByUtilisateurIdAndDateFinIsNullAndDateDebutLessThanEqual(utilisateur.getId(), debut)
                    .stream().anyMatch(a -> roleSalarie.getId().equals(a.getRole().getId()));
            if (roleSalarie != null && !attributionSalarieExistante) {
                UtilisateurRole attribution = UtilisateurRole.nouvelle();
                attribution.setUtilisateur(utilisateur);
                attribution.setRole(roleSalarie);
                attribution.setDateDebut(debut);
                attribution.setMotif("Conversion du compte candidat après acceptation de la proposition");
                attributions.save(attribution);
            }
        });

        Contrat contrat = Contrat.nouvelle();
        contrat.setEmploye(employe);
        contrat.setPoste(poste);
        contrat.setTypeContrat(type);
        contrat.setTypeActe(ContratActe.INITIAL);
        contrat.setDateDebut(debut);
        contrat.setDateFin(fin);
        contrat.setSalaireBrut(salaire);
        contrat.setDevise("MGA");
        contrat.setStatutContrat(debut.isAfter(LocalDate.now()) ? ContratStatut.EN_ATTENTE : ContratStatut.EN_COURS);
        contrats.saveAndFlush(contrat);

        if (demande.getTypeDemande() == com.itu.gestionRH.entity.enums.TypeDemande.REMPLACEMENT
                && demande.getEmployeRemplaceId() != null) {
            affectations.findFirstByEmployeIdAndPosteIdAndDateFinIsNullOrderByDateDebutDesc(demande.getEmployeRemplaceId(), poste.getId())
                    .ifPresent(ancienne -> {
                        ancienne.setDateFin(debut.isBefore(ancienne.getDateDebut()) ? ancienne.getDateDebut() : debut);
                        affectations.save(ancienne);
                    });
        }

        if (poste.getStatutPoste() == PosteStatut.CREE)
            posteMachine.transition(poste, "ouvrir", contexteSysteme());
        Affectation affectation = Affectation.nouvelle();
        affectation.setEmploye(employe);
        affectation.setPoste(poste);
        affectation.setContrat(contrat);
        affectation.setDateDebut(debut);
        affectation.setMotif("Embauche après acceptation de la proposition");
        var manager = demande.getManager();
        if (manager != null && manager.getEmployeId() != null)
            employes.findById(manager.getEmployeId()).ifPresent(affectation::setSuperieur);
        affectations.saveAndFlush(affectation);
        poste.getAffectations().add(affectation);
        if (poste.getStatutPoste() == PosteStatut.VACANT)
            posteMachine.transition(poste, "pourvoir", contexteSysteme());
        postes.saveAndFlush(poste);

        proposition.setEmploye(employe);
        return employe;
    }

    private WorkflowContext contexteSysteme() {
        return WorkflowContext.of("SYSTEME", Set.of(), Set.of("CONTRAT_GERER", "UTILISATEUR_ADMIN"), "Finalisation automatique de l’embauche");
    }
}
