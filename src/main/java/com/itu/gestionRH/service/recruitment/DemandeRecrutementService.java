package com.itu.gestionRH.service.recruitment;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.controller.recruitment.dto.DemandeDtos.*;
import com.itu.gestionRH.entity.enums.DecisionValidation;
import com.itu.gestionRH.entity.enums.DemandeStatut;
import com.itu.gestionRH.entity.enums.EtapeValidation;
import com.itu.gestionRH.entity.enums.PosteStatut;
import com.itu.gestionRH.entity.enums.TypeDemande;
import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.entity.recruitment.DemandeValidation;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.repository.recruitment.DemandeValidationRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.workflow.DemandeRecrutementMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DemandeRecrutementService {
    private final DemandeRecrutementRepository demandes;
    private final DemandeValidationRepository validations;
    private final PosteRepository postes;
    private final EmployeRepository employes;
    private final UtilisateurRepository utilisateurs;
    private final DemandeRecrutementMachine machine;
    private final AccessScope accessScope;

    public DemandeRecrutementService(DemandeRecrutementRepository demandes, DemandeValidationRepository validations,
            PosteRepository postes, EmployeRepository employes, UtilisateurRepository utilisateurs,
            DemandeRecrutementMachine machine, AccessScope accessScope) {
        this.demandes=demandes; this.validations=validations; this.postes=postes; this.employes=employes;
        this.utilisateurs=utilisateurs; this.machine=machine; this.accessScope=accessScope;
    }

    @Transactional
    public DemandeResponse creer(DemandeCreateRequest request) {
        WorkflowContext contexte=contexte();
        exigerManager(contexte);
        var poste=posteActif(request.posteId());
        validerMetier(request.typeDemande(), request.employeRemplaceId(), request.effectif(), request.budget());
        Utilisateur manager=utilisateurs.findById(contexte.utilisateurId()).orElseThrow(() -> new NotFoundException("Utilisateur introuvable."));
        DemandeRecrutement demande=DemandeRecrutement.nouvelle();
        appliquer(demande, request.typeDemande(), request.employeRemplaceId(), request.effectif(), request.motif(), request.budget(), request.dateSouhaitee());
        demande.setPoste(poste); demande.setManager(manager); demande.initialiserEnBrouillon();
        demande=demandes.saveAndFlush(demande);
        if (Boolean.TRUE.equals(request.soumettreDirectement())) {
            machine.transition(demande, "soumettre", contexte);
            validations.save(validation(demande, EtapeValidation.RH, DecisionValidation.VALIDEE, manager,
                    "Soumission initiale par le manager (entrée dans le circuit RH)."));
        }
        return reponse(demande, contexte);
    }

    @Transactional
    public DemandeResponse modifier(Long id, DemandeUpdateRequest request) {
        WorkflowContext contexte=contexte();
        DemandeRecrutement demande=visible(id, contexte);
        exigerProprietaire(demande, contexte);
        if (demande.getStatutValidation() != DemandeStatut.BROUILLON) throw erreur("DEMANDE_NON_MODIFIABLE", "Seule une demande en brouillon peut être modifiée.", HttpStatus.CONFLICT);
        var poste=posteActif(request.posteId());
        validerMetier(request.typeDemande(), request.employeRemplaceId(), request.effectif(), request.budget());
        appliquer(demande, request.typeDemande(), request.employeRemplaceId(), request.effectif(), request.motif(), request.budget(), request.dateSouhaitee());
        demande.setPoste(poste);
        return reponse(demandes.save(demande), contexte);
    }

    public PageResponse<DemandeResponse> lister(DemandeStatut statut, Long departementId, Long managerId,
            java.time.LocalDate dateDebut, java.time.LocalDate dateFin, String texte, Pageable pageable) {
        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) throw erreur("PERIODE_INVALIDE", "La date de fin doit être postérieure à la date de début.", HttpStatus.BAD_REQUEST);
        WorkflowContext contexte=contexte();
        Long managerScope=contexte.roles().contains("MANAGER") || !peutConsulterToutes(contexte) ? contexte.utilisateurId() : null;
        Pageable pagination=PageRequest.of(Math.max(0, pageable.getPageNumber()), Math.min(100, Math.max(1, pageable.getPageSize())), pageable.getSort());
        Page<DemandeResponse> page=demandes.rechercher(statut, departementId, managerId, managerScope,
                dateDebut, dateFin, normaliser(texte), pagination).map(d -> reponse(d, contexte));
        return PageResponse.from(page);
    }

    public DemandeDetailResponse obtenir(Long id) {
        WorkflowContext contexte=contexte();
        DemandeRecrutement demande=visible(id, contexte);
        var historique=validations.findByDemandeIdOrderByDateDecisionAscIdAsc(id).stream().map(v ->
                new ValidationDto(v.getId(), v.getEtape().name(), v.getDecision().name(),
                        v.getValidateur().getId(), v.getValidateur().getNomAffichage(), v.getDateDecision(), v.getJustification())).toList();
        var offresLiees=demande.getOffresEmplois().stream().map(o -> new OffreDto(o.getId(), o.getTitre(), o.getStatutOffre().name(), o.getDatePublication())).toList();
        return new DemandeDetailResponse(reponse(demande, contexte), historique, offresLiees,
                machine.actionsDisponibles(demande, contexte));
    }

    @Transactional
    public DemandeResponse soumettre(Long id) {
        WorkflowContext contexte=contexte();
        DemandeRecrutement demande=visible(id, contexte);
        exigerManager(contexte); exigerProprietaire(demande, contexte);
        machine.transition(demande, "soumettre", contexte);
        Utilisateur manager=demande.getManager();
        validations.save(validation(demande, EtapeValidation.RH, DecisionValidation.VALIDEE, manager,
                "Soumission initiale par le manager (entrée dans le circuit RH)."));
        return reponse(demande, contexte);
    }

    @Transactional
    public DemandeResponse traiterValidation(Long id, ValidationRequest request) {
        WorkflowContext contexte=contexte();
        DemandeRecrutement demande=visible(id, contexte);
        if (contexte.utilisateurId() != null && contexte.utilisateurId().equals(demande.getManager().getId()))
            throw erreur("DEMANDE_AUTO_VALIDATION_INTERDITE", "Un manager ne peut pas valider sa propre demande.", HttpStatus.FORBIDDEN);
        if (demande.getStatutValidation() == DemandeStatut.ATTENTE_RH && !contexte.roles().contains("CHARGE_RECRUTEMENT"))
            throw erreur("ROLE_CHARGE_RECRUTEMENT_REQUIS", "Cette étape est réservée aux chargés de recrutement.", HttpStatus.FORBIDDEN);
        if (demande.getStatutValidation() == DemandeStatut.ATTENTE_DIRECTION && !contexte.roles().contains("DIRECTION"))
            throw erreur("ROLE_DIRECTION_REQUIS", "Cette étape est réservée à la Direction.", HttpStatus.FORBIDDEN);
        String action=actionMachine(request.action());
        boolean refus=action.equals("refuser");
        String justification=normaliser(request.justification());
        if (refus && justification == null) throw erreur("JUSTIFICATION_OBLIGATOIRE", "Une justification est obligatoire pour cette action.", HttpStatus.BAD_REQUEST);
        if (demande.getStatutValidation() == DemandeStatut.ATTENTE_RH) verifierConformiteRh(demande);
        DemandeStatut avant=demande.getStatutValidation();
        machine.transition(demande, action, new WorkflowContext(contexte.acteur(), contexte.utilisateurId(),
                contexte.roles(), contexte.permissions(), justification, contexte.attributs()));
        EtapeValidation etape=avant == DemandeStatut.ATTENTE_RH ? EtapeValidation.RH : EtapeValidation.DIRECTION;
        DecisionValidation decision=action.equals("refuser") ? DecisionValidation.REFUSEE : DecisionValidation.VALIDEE;
        Utilisateur validateur=utilisateurs.findById(contexte.utilisateurId()).orElseThrow(() -> new NotFoundException("Utilisateur introuvable."));
        validations.save(validation(demande, etape, decision, validateur, justification));
        return reponse(demande, contexte);
    }

    @Transactional
    public void supprimer(Long id) {
        WorkflowContext contexte=contexte();
        DemandeRecrutement demande=visible(id, contexte);
        exigerManager(contexte); exigerProprietaire(demande, contexte);
        if (demande.getStatutValidation() != DemandeStatut.BROUILLON) throw erreur("DEMANDE_NON_SUPPRIMABLE", "Seule une demande en brouillon peut être supprimée.", HttpStatus.CONFLICT);
        if (validations.countByDemandeId(id) > 0 || !demande.getOffresEmplois().isEmpty())
            throw erreur("DEMANDE_HISTORISEE_NON_SUPPRIMABLE", "Cette demande possède un historique ou une offre liée et ne peut pas être supprimée.", HttpStatus.CONFLICT);
        demandes.delete(demande);
    }

    public SyntheseResponse synthese() {
        WorkflowContext contexte=contexte();
        Long managerScope=contexte.roles().contains("MANAGER") || !peutConsulterToutes(contexte) ? contexte.utilisateurId() : null;
        EnumMap<DemandeStatut,Long> compteur=new EnumMap<>(DemandeStatut.class);
        for (DemandeStatut statut : DemandeStatut.values()) {
            long count=managerScope == null ? demandes.countByStatutValidation(statut) : demandes.countByManagerIdAndStatutValidation(managerScope, statut);
            compteur.put(statut, count);
        }
        var recente=demandes.rechercher(null, null, managerScope, managerScope, null, null, null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateCreation"))).map(d -> reponse(d, contexte)).getContent();
        long total=compteur.values().stream().mapToLong(Long::longValue).sum();
        Map<String,Long> parStatut=compteur.entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        return new SyntheseResponse(total, recente, parStatut);
    }

    private void verifierConformiteRh(DemandeRecrutement demande) {
        if (demande.getPoste() == null || demande.getPoste().getStatutPoste() == PosteStatut.FERME
                || Boolean.FALSE.equals(demande.getPoste().getDepartement().getEstActif()))
            throw erreur("POSTE_INACTIF", "Le poste ou son département n'est plus actif.", HttpStatus.UNPROCESSABLE_ENTITY);
        validerMetier(demande.getTypeDemande(), demande.getEmployeRemplaceId(), demande.getEffectifDemande(), demande.getBudgetAlloue());
    }

    private com.itu.gestionRH.entity.organization.Poste posteActif(Long id) {
        var poste=postes.findById(id).orElseThrow(() -> new NotFoundException("Poste introuvable."));
        if (poste.getStatutPoste() == PosteStatut.FERME || Boolean.FALSE.equals(poste.getDepartement().getEstActif()))
            throw erreur("POSTE_INACTIF", "Le poste ou son département n'est plus actif.", HttpStatus.UNPROCESSABLE_ENTITY);
        return poste;
    }

    private void validerMetier(TypeDemande type, Long employeId, Integer effectif, java.math.BigDecimal budget) {
        if (type == null) throw erreur("TYPE_DEMANDE_REQUIS", "Le type de demande est obligatoire.", HttpStatus.BAD_REQUEST);
        if (type == TypeDemande.REMPLACEMENT && employeId == null) throw erreur("EMPLOYE_REMPLACE_REQUIS", "Un employé à remplacer est obligatoire.", HttpStatus.BAD_REQUEST);
        if (type == TypeDemande.CREATION && employeId != null) throw erreur("EMPLOYE_REMPLACE_INATTENDU", "Une demande de création ne doit pas désigner d'employé remplacé.", HttpStatus.BAD_REQUEST);
        if (type == TypeDemande.REMPLACEMENT && !employes.existsById(employeId)) throw new NotFoundException("Employé à remplacer introuvable.");
        if (effectif == null || effectif <= 0) throw erreur("EFFECTIF_INVALIDE", "L'effectif demandé doit être supérieur à zéro.", HttpStatus.BAD_REQUEST);
        if (budget != null && budget.signum() < 0) throw erreur("BUDGET_INVALIDE", "Le budget ne peut pas être négatif.", HttpStatus.BAD_REQUEST);
    }

    private void appliquer(DemandeRecrutement demande, TypeDemande type, Long employeId,
            Integer effectif, String motif, java.math.BigDecimal budget, java.time.LocalDate date) {
        demande.setTypeDemande(type); demande.setEmployeRemplaceId(employeId); demande.setEffectifDemande(effectif);
        demande.setMotif(motif.trim()); demande.setBudgetAlloue(budget); demande.setDateSouhaitee(date);
    }

    private DemandeValidation validation(DemandeRecrutement demande, EtapeValidation etape,
            DecisionValidation decision, Utilisateur validateur, String justification) {
        DemandeValidation validation=DemandeValidation.nouvelle();
        validation.setDemande(demande); validation.setEtape(etape); validation.setDecision(decision);
        validation.setValidateur(validateur); validation.setDateDecision(LocalDateTime.now());
        validation.setJustification(justification);
        return validation;
    }

    private DemandeRecrutement visible(Long id, WorkflowContext contexte) {
        if (contexte.roles().contains("MANAGER")) {
            DemandeRecrutement d=demandes.findById(id).orElseThrow(() -> new NotFoundException("Demande introuvable."));
            if (contexte.utilisateurId() == null || d.getManager() == null || !contexte.utilisateurId().equals(d.getManager().getId()))
                throw new NotFoundException("Demande introuvable.");
            return d;
        }
        return accessScope.requireDemande(id);
    }

    private void exigerManager(WorkflowContext contexte) {
        if (!contexte.roles().contains("MANAGER") || contexte.utilisateurId() == null)
            throw erreur("ROLE_MANAGER_REQUIS", "Cette action est réservée au manager connecté.", HttpStatus.FORBIDDEN);
    }
    private void exigerProprietaire(DemandeRecrutement demande, WorkflowContext contexte) {
        if (demande.getManager() == null || contexte.utilisateurId() == null || !contexte.utilisateurId().equals(demande.getManager().getId()))
            throw new NotFoundException("Demande introuvable.");
    }
    private boolean peutConsulterToutes(WorkflowContext ctx) {
        return ctx.permissions().contains("DEMANDE_VERIFIER_RH") || ctx.permissions().contains("DEMANDE_APPROUVER_DIR");
    }
    private String actionMachine(String action) {
        return switch (action) {
            case "TRANSMETTRE_DIRECTION" -> "transmettre_direction";
            case "VALIDER" -> "valider";
            case "REFUSER" -> "refuser";
            case "RENVOYER_MANAGER" -> "renvoyer_manager";
            default -> throw erreur("ACTION_VALIDATION_INVALIDE", "L'action de validation n'est pas reconnue.", HttpStatus.BAD_REQUEST);
        };
    }
    private WorkflowContext contexte() { return WorkflowContext.utilisateurCourant(null); }
    private DemandeResponse reponse(DemandeRecrutement d, WorkflowContext context) {
        var poste=d.getPoste(); var departement=poste == null ? null : poste.getDepartement();
        return new DemandeResponse(d.getId(), poste == null ? null : poste.getId(), poste == null ? null : poste.getIntitule(),
                departement == null ? null : departement.getId(), departement == null ? null : departement.getLibelle(),
                d.getManager() == null ? null : d.getManager().getId(), d.getManager() == null ? null : d.getManager().getNomAffichage(),
                d.getTypeDemande(), d.getEmployeRemplaceId(), d.getEffectifDemande(), d.getMotif(), d.getBudgetAlloue(),
                d.getDateSouhaitee(), d.getStatutValidation(), d.getDateCreation(), machine.actionsDisponibles(d, context));
    }
    private static String normaliser(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static BusinessRuleException erreur(String code, String message, HttpStatus status) { return new BusinessRuleException(code, message, status); }
}
