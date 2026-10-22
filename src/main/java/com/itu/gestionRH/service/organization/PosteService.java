package com.itu.gestionRH.service.organization;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.controller.organization.OrganisationDtos.PosteDto;
import com.itu.gestionRH.controller.organization.OrganisationDtos.PosteRequest;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.PosteStatut;
import com.itu.gestionRH.entity.organization.Departement;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.organization.DepartementRepository;
import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.workflow.PosteMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PosteService {
    private final PosteRepository postes;
    private final DepartementRepository departements;
    private final ContratRepository contrats;
    private final AffectationRepository affectations;
    private final PosteMachine machine;
    public PosteService(PosteRepository postes, DepartementRepository departements, ContratRepository contrats,
            AffectationRepository affectations, PosteMachine machine) {
        this.postes=postes; this.departements=departements; this.contrats=contrats; this.affectations=affectations; this.machine=machine;
    }

    @Transactional
    public Page<PosteDto> lister(Long departementId, PosteStatut statut, String recherche, Pageable pageable) {
        String q = recherche == null || recherche.isBlank() ? null : recherche.toLowerCase(Locale.ROOT).trim();
        List<Poste> result = postes.findAll(Sort.by(Sort.Direction.ASC, "intitule")).stream()
                .filter(p -> departementId == null || p.getDepartement().getId().equals(departementId))
                .filter(p -> q == null || p.getCodePoste().toLowerCase(Locale.ROOT).contains(q)
                        || p.getIntitule().toLowerCase(Locale.ROOT).contains(q)
                        || (p.getDescription() != null && p.getDescription().toLowerCase(Locale.ROOT).contains(q)))
                .filter(p -> statut == null || statutEffectif(p).equals(statut)).toList();
        int start = (int) Math.min(pageable.getOffset(), result.size());
        int end = Math.min(start + pageable.getPageSize(), result.size());
        return new PageImpl<>(result.subList(start, end).stream().map(this::dto).toList(), pageable, result.size());
    }

    @Transactional(readOnly=true)
    public PosteDto obtenir(Long id) { return dto(trouver(id)); }

    public PosteDto creer(PosteRequest request) {
        if (postes.findByCodePoste(request.codePoste()) != null) throw new BusinessRuleException("POSTE_CODE_EXISTANT", "Ce code poste existe déjà.");
        Departement departement = departements.findById(request.departementId()).orElseThrow(() -> new NotFoundException("Département introuvable."));
        if (!Boolean.TRUE.equals(departement.getEstActif())) throw new BusinessRuleException("DEPARTEMENT_INACTIF", "Un poste doit être rattaché à un département actif.");
        Poste poste = Poste.nouvelle();
        poste.initialiser(request.codePoste().trim(), request.intitule().trim(), request.description(), departement);
        postes.saveAndFlush(poste);
        machine.transition(poste, "ouvrir", WorkflowContext.utilisateurCourant(null));
        return dto(postes.saveAndFlush(poste));
    }

    public PosteDto modifier(Long id, PosteRequest request) {
        Poste poste = trouver(id);
        if (postes.findByCodePoste(request.codePoste()) != null && !postes.findByCodePoste(request.codePoste()).getId().equals(id))
            throw new BusinessRuleException("POSTE_CODE_EXISTANT", "Ce code poste existe déjà.");
        Departement departement = departements.findById(request.departementId()).orElseThrow(() -> new NotFoundException("Département introuvable."));
        poste.setCodePoste(request.codePoste().trim()); poste.setIntitule(request.intitule().trim());
        poste.setDescription(request.description()); poste.setDepartement(departement);
        return dto(postes.saveAndFlush(poste));
    }

    /** Les postes sont conservés pour préserver les références métier : DELETE signifie fermeture. */
    public PosteDto fermer(Long id) {
        Poste poste = trouver(id);
        if (poste.getStatutPoste() != PosteStatut.FERME) {
            machine.transition(poste, "fermer", WorkflowContext.utilisateurCourant("Fermeture du poste"));
            postes.saveAndFlush(poste);
        }
        return dto(poste);
    }

    @Transactional
    public WorkflowTransitionEvent transition(Long id, String action, WorkflowContext contexte) {
        Poste poste = trouver(id);
        WorkflowTransitionEvent event = machine.transition(poste, action, contexte);
        postes.saveAndFlush(poste);
        return event;
    }

    private Poste trouver(Long id) { return postes.findById(id).orElseThrow(() -> new NotFoundException("Poste introuvable.")); }
    private PosteStatut statutEffectif(Poste poste) {
        if (poste.getStatutPoste() == PosteStatut.FERME) return PosteStatut.FERME;
        if (contrats.findByPosteIdAndStatutContrat(poste.getId(), ContratStatut.EN_COURS).size() > 0
                || affectations.existsByPosteIdAndDateFinIsNull(poste.getId())) return PosteStatut.POURVU;
        return poste.getStatutPoste() == PosteStatut.CREE ? PosteStatut.VACANT : poste.getStatutPoste();
    }
    private PosteDto dto(Poste p) {
        return new PosteDto(p.getId(), p.getCodePoste(), p.getIntitule(), p.getDescription(), p.getDepartement().getId(),
                p.getDepartement().getLibelle(), statutEffectif(p), machine.actionsDisponibles(p, WorkflowContext.utilisateurCourant(null)));
    }
}
