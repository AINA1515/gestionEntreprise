package com.itu.gestionRH.security;

import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.recruitment.Candidature;
import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import com.itu.gestionRH.entity.documents.Document;
import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.DocumentStatut;
import com.itu.gestionRH.entity.enums.DocumentConfidentialite;
import com.itu.gestionRH.repository.documents.DocumentRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.hiring.PropositionEmbaucheRepository;
import com.itu.gestionRH.repository.onboarding.OnboardingTacheRepository;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.jwt.JwtAuthenticationFilter.JwtPrincipal;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Data-level authorization. Out-of-scope IDs deliberately look like missing resources. */
@Component
@Transactional(readOnly = true)
public class AccessScope {
    private final DemandeRecrutementRepository demandes;
    private final CandidatureRepository candidatures;
    private final UtilisateurRepository utilisateurs;
    private final AffectationRepository affectations;
    private final ContratRepository contrats;
    private final OnboardingTacheRepository taches;
    private final PropositionEmbaucheRepository propositions;
    private final DocumentRepository documents;
    public AccessScope(DemandeRecrutementRepository demandes, CandidatureRepository candidatures,
            UtilisateurRepository utilisateurs, AffectationRepository affectations, ContratRepository contrats,
            OnboardingTacheRepository taches, PropositionEmbaucheRepository propositions, DocumentRepository documents) {
        this.demandes=demandes; this.candidatures=candidatures; this.utilisateurs=utilisateurs; this.affectations=affectations;
        this.contrats=contrats; this.taches=taches; this.propositions=propositions; this.documents=documents;
    }

    public DemandeRecrutement requireDemande(Long id) {
        DemandeRecrutement d=demandes.findById(id).orElseThrow(()->new NotFoundException("Demande introuvable."));
        if (has("PERM_DEMANDE_VERIFIER_RH") || has("PERM_DEMANDE_APPROUVER_DIR") || has("PERM_UTILISATEUR_ADMIN")) return d;
        if (has("PERM_DEMANDE_CREER") && userId()!=null && d.getManager()!=null && userId().equals(d.getManager().getId())) return d;
        throw new NotFoundException("Demande introuvable.");
    }

    public List<DemandeRecrutement> demandesVisibles() {
        if (has("PERM_DEMANDE_VERIFIER_RH") || has("PERM_DEMANDE_APPROUVER_DIR") || has("PERM_UTILISATEUR_ADMIN")) return demandes.findAll();
        if (has("PERM_DEMANDE_CREER") && userId()!=null) return demandes.findAll().stream()
                .filter(d->d.getManager()!=null && userId().equals(d.getManager().getId())).toList();
        return List.of();
    }

    public Candidature requireCandidature(Long id) {
        Candidature c=candidatures.findById(id).orElseThrow(()->new NotFoundException("Candidature introuvable."));
        Utilisateur u=currentUser();
        if (u!=null && u.getCandidatId()!=null && c.getCandidat()!=null && u.getCandidatId().equals(c.getCandidat().getId())) return c;
        if (has("PERM_DEMANDE_CREER")) {
            if(c.getOffre()!=null && c.getOffre().getDemande()!=null
                && c.getOffre().getDemande().getManager()!=null
                && userId()!=null && userId().equals(c.getOffre().getDemande().getManager().getId())) return c;
            throw new NotFoundException("Candidature introuvable.");
        }
        if (has("PERM_UTILISATEUR_ADMIN") || has("PERM_CANDIDATURE_PRESELECTIONNER") || has("PERM_EVALUATION_SAISIR")) return c;
        throw new NotFoundException("Candidature introuvable.");
    }

    public boolean canSeeCandidature(Candidature c) {
        try { requireCandidature(c.getId()); return true; }
        catch (NotFoundException ignored) { return false; }
    }

    public void requireEmploye(Long id) {
        Utilisateur u=currentUser();
        if (has("PERM_CONTRAT_GERER") || has("PERM_EMPLOYE_CONVERTIR") || has("PERM_ONBOARDING_ADMIN")
                || has("PERM_UTILISATEUR_ADMIN") || has("PERM_DOCUMENT_CONSULTER_TOUS") || has("PERM_REPORTING_CONSULTER")) return;
        if(u!=null && id.equals(u.getEmployeId())) return;
        if(u!=null && u.getEmployeId()!=null && has("PERM_DEMANDE_CREER")
                && affectations.existsByEmployeIdAndSuperieurIdAndDateFinIsNull(id,u.getEmployeId())) return;
        throw new NotFoundException("Ressource introuvable.");
    }

    public Long requireCandidatCourant() {
        Utilisateur u=currentUser();
        if(u==null || u.getCandidatId()==null) throw new NotFoundException("Ressource introuvable.");
        return u.getCandidatId();
    }

    public Long currentUserId() {
        Long id=userId();
        if(id==null) throw new NotFoundException("Ressource introuvable.");
        return id;
    }

    public PropositionEmbauche requirePropositionOwner(PropositionEmbauche proposition) {
        if(proposition==null) throw new NotFoundException("Proposition introuvable.");
        if(has("PERM_UTILISATEUR_ADMIN") || has("PERM_PROPOSITION_EMETTRE")) return proposition;
        Long candidat=requireCandidatCourant();
        if(proposition.getCandidature()!=null && proposition.getCandidature().getCandidat()!=null
                && candidat.equals(proposition.getCandidature().getCandidat().getId())) return proposition;
        throw new NotFoundException("Proposition introuvable.");
    }

    public OnboardingTache requireTaskOwner(OnboardingTache task) {
        if(task==null) throw new NotFoundException("Tâche introuvable.");
        if(has("PERM_UTILISATEUR_ADMIN")) return task;
        Long id=userId();
        if(id!=null && id.equals(task.getAssigneeAId())) return task;
        throw new NotFoundException("Tâche introuvable.");
    }

    public Document requireDocument(Long id) {
        Document d=documents.findById(id).orElseThrow(()->new NotFoundException("Document introuvable."));
        if (d.getStatutDocument() == DocumentStatut.SUPPRIME) throw new NotFoundException("Document introuvable.");
        if(has("PERM_DOCUMENT_CONSULTER_TOUS") || has("PERM_UTILISATEUR_ADMIN")) return d;
        if (d.getConfidentialite() == DocumentConfidentialite.STRICT) throw new NotFoundException("Document introuvable.");
        if (has("PERM_DOCUMENT_CONSULTER_PERSO") && estProprietaire(d)) return d;
        try {
            DocumentEntite type=d.getEntiteRattachement();
            switch(type) {
                case CANDIDAT -> { if (candidatLieAuManagerOuRecruteur(d.getEntiteId())) return d; }
                case CANDIDATURE -> { return requireDocumentViaCandidature(d); }
                case EMPLOYE -> { requireEmploye(d.getEntiteId()); return d; }
                case CONTRAT -> { var c=contrats.findById(d.getEntiteId()).orElseThrow(()->new NotFoundException("Document introuvable.")); requireEmploye(c.getEmploye().getId()); return d; }
                case ONBOARDING_TACHE -> { var t=taches.findById(d.getEntiteId()).orElseThrow(()->new NotFoundException("Document introuvable.")); requireTaskOwner(t); return d; }
                case PROPOSITION_EMBAUCHE -> { var p=propositions.findById(d.getEntiteId()).orElseThrow(()->new NotFoundException("Document introuvable.")); requirePropositionOwner(p); return d; }
                case POSTE -> { if (has("PERM_DEMANDE_CREER") || has("PERM_OFFRE_PUBLIER") || has("PERM_CONTRAT_GERER")) return d; }
            }
        } catch(NotFoundException ignored) { throw new NotFoundException("Document introuvable."); }
        throw new NotFoundException("Document introuvable.");
    }

    public void requireDocumentTarget(DocumentEntite entite, Long entiteId) {
        if (has("PERM_DOCUMENT_CONSULTER_TOUS") || has("PERM_UTILISATEUR_ADMIN")) return;
        if (entite == null || entiteId == null) throw new NotFoundException("Ressource introuvable.");
        try {
            switch (entite) {
                case CANDIDAT -> {
                    if (has("PERM_DOCUMENT_CONSULTER_PERSO") && entiteId.equals(requireCandidatCourant())) return;
                    if (candidatLieAuManagerOuRecruteur(entiteId)) return;
                }
                case CANDIDATURE -> { requireCandidature(entiteId); return; }
                case EMPLOYE -> { requireEmploye(entiteId); return; }
                case CONTRAT -> {
                    var c = contrats.findById(entiteId).orElseThrow(() -> new NotFoundException("Ressource introuvable."));
                    requireEmploye(c.getEmploye().getId()); return;
                }
                case ONBOARDING_TACHE -> {
                    var t = taches.findById(entiteId).orElseThrow(() -> new NotFoundException("Ressource introuvable."));
                    requireTaskOwner(t); return;
                }
                case PROPOSITION_EMBAUCHE -> {
                    var p = propositions.findById(entiteId).orElseThrow(() -> new NotFoundException("Ressource introuvable."));
                    requirePropositionOwner(p); return;
                }
                case POSTE -> { if (has("PERM_DEMANDE_CREER") || has("PERM_OFFRE_PUBLIER") || has("PERM_CONTRAT_GERER")) return; }
            }
        } catch (NotFoundException ignored) { throw new NotFoundException("Ressource introuvable."); }
        throw new NotFoundException("Ressource introuvable.");
    }

    private Document requireDocumentViaCandidature(Document d) { requireCandidature(d.getEntiteId()); return d; }

    private boolean estProprietaire(Document d) {
        Utilisateur u = currentUser();
        if (u == null) return false;
        return switch (d.getEntiteRattachement()) {
            case CANDIDAT -> u.getCandidatId() != null && u.getCandidatId().equals(d.getEntiteId());
            case EMPLOYE -> u.getEmployeId() != null && u.getEmployeId().equals(d.getEntiteId());
            case CANDIDATURE -> estCandidatDeCandidature(d.getEntiteId(), u);
            case CONTRAT -> contrats.findById(d.getEntiteId()).map(c -> u.getEmployeId() != null && u.getEmployeId().equals(c.getEmploye().getId())).orElse(false);
            case ONBOARDING_TACHE -> taches.findById(d.getEntiteId()).map(t -> userId() != null && userId().equals(t.getAssigneeAId())).orElse(false);
            case PROPOSITION_EMBAUCHE -> propositions.findById(d.getEntiteId()).map(p -> u.getCandidatId() != null && p.getCandidature() != null
                    && p.getCandidature().getCandidat() != null && u.getCandidatId().equals(p.getCandidature().getCandidat().getId())).orElse(false);
            case POSTE -> false;
        };
    }

    private boolean estCandidatDeCandidature(Long candidatureId, Utilisateur u) {
        return candidatures.findById(candidatureId).map(c -> u.getCandidatId() != null && c.getCandidat() != null
                && u.getCandidatId().equals(c.getCandidat().getId())).orElse(false);
    }

    private boolean candidatLieAuManagerOuRecruteur(Long candidatId) {
        if (!(has("PERM_DEMANDE_CREER") || has("PERM_CANDIDATURE_PRESELECTIONNER") || has("PERM_EVALUATION_SAISIR"))) return false;
        return candidatures.findByCandidatId(candidatId, org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .anyMatch(c -> canSeeCandidature(c));
    }

    private Utilisateur currentUser(){Long id=userId();return id==null?null:utilisateurs.findById(id).orElse(null);}
    private Long userId(){Authentication a=auth();return a!=null && a.getPrincipal() instanceof JwtPrincipal p?p.userId():null;}
    private Authentication auth(){return SecurityContextHolder.getContext().getAuthentication();}
    private boolean has(String authority){Authentication a=auth();return a!=null&&a.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority::equals);}
}
