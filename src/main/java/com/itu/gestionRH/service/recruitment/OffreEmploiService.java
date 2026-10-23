package com.itu.gestionRH.service.recruitment;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.controller.recruitment.dto.OffreDtos.OffreCreateRequest;
import com.itu.gestionRH.controller.recruitment.dto.OffreDtos.OffreUpdateRequest;
import com.itu.gestionRH.entity.enums.OffreDiffusion;
import com.itu.gestionRH.entity.enums.OffreStatut;
import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.repository.recruitment.OffreEmploiRepository;
import com.itu.gestionRH.workflow.OffreEmploiMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.workflow.WorkflowTransitionEvent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OffreEmploiService {
    private static final List<OffreStatut> STATUTS_COUVRANTS = List.of(OffreStatut.BROUILLON, OffreStatut.PUBLIEE);

    private final OffreEmploiRepository offres;
    private final DemandeRecrutementRepository demandes;
    private final CandidatureRepository candidatures;
    private final OffreEmploiMachine machine;

    public OffreEmploiService(OffreEmploiRepository offres, DemandeRecrutementRepository demandes,
            CandidatureRepository candidatures, OffreEmploiMachine machine) {
        this.offres = offres;
        this.demandes = demandes;
        this.candidatures = candidatures;
        this.machine = machine;
    }

    @Transactional
    public OffreEmploi creer(OffreCreateRequest request) {
        var demande = demandes.findById(request.demandeId())
                .orElseThrow(() -> new NotFoundException("Demande de recrutement introuvable."));
        if (demande.getStatutValidation() != com.itu.gestionRH.entity.enums.DemandeStatut.VALIDEE) {
            throw erreur("DEMANDE_NON_VALIDEE", "Une offre ne peut être créée qu'à partir d'une demande validée.", HttpStatus.CONFLICT);
        }
        long offresExistantes = offres.countByDemandeIdAndStatutOffreIn(demande.getId(), STATUTS_COUVRANTS);
        if (offresExistantes >= demande.getEffectifDemande()) {
            throw erreur("EFFECTIF_DEMANDE_COUVERT", "L'effectif demandé est déjà couvert par des offres ouvertes.", HttpStatus.CONFLICT);
        }
        verifierDateLimite(request.dateLimiteCandidature());

        OffreEmploi offre = OffreEmploi.nouvelle();
        offre.setDemande(demande);
        offre.setTitre(texteOu(request.titre(), demande.getPoste().getIntitule()));
        offre.setDescriptionPoste(texteOu(request.description(), demande.getPoste().getDescription()));
        offre.setProfilRecherche(netoyer(request.profilRecherche()));
        offre.setDiffusion(request.diffusion() == null ? OffreDiffusion.EXTERNE : request.diffusion());
        offre.setDateLimiteCandidature(request.dateLimiteCandidature());
        offre.setLieu(netoyer(request.lieu()));
        offre.setTypeContrat(request.typeContrat());
        offre.setTempsTravail(request.tempsTravail());
        offre.initialiserEnBrouillon();
        return offres.saveAndFlush(offre);
    }

    @Transactional
    public OffreEmploi modifier(Long id, OffreUpdateRequest request) {
        OffreEmploi offre = trouver(id);
        exigerBrouillon(offre);
        verifierDateLimite(request.dateLimiteCandidature());
        if (request.titre() != null) offre.setTitre(request.titre().trim());
        if (request.description() != null) offre.setDescriptionPoste(request.description().trim());
        if (request.profilRecherche() != null) offre.setProfilRecherche(request.profilRecherche().trim());
        if (request.diffusion() != null) offre.setDiffusion(request.diffusion());
        offre.setDateLimiteCandidature(request.dateLimiteCandidature());
        if (request.lieu() != null) offre.setLieu(request.lieu().trim());
        if (request.typeContrat() != null) offre.setTypeContrat(request.typeContrat());
        if (request.tempsTravail() != null) offre.setTempsTravail(request.tempsTravail());
        if (offre.getTitre() == null || offre.getTitre().isBlank()) {
            throw erreur("OFFRE_TITRE_REQUIS", "Le titre de l'offre est obligatoire.", HttpStatus.BAD_REQUEST);
        }
        return offres.saveAndFlush(offre);
    }

    @Transactional
    public WorkflowTransitionEvent publier(Long id, WorkflowContext contexte) {
        OffreEmploi offre = trouver(id);
        exigerBrouillon(offre);
        verifierDateLimite(offre.getDateLimiteCandidature());
        offre.setDatePublication(LocalDate.now());
        WorkflowTransitionEvent event = machine.transition(offre, "publier", contexte);
        offres.saveAndFlush(offre);
        return event;
    }

    @Transactional
    public WorkflowTransitionEvent cloturer(Long id, WorkflowContext contexte) {
        OffreEmploi offre = trouver(id);
        offre.setDateCloture(LocalDate.now());
        WorkflowTransitionEvent event = machine.transition(offre, "cloturer", contexte);
        offres.saveAndFlush(offre);
        return event;
    }

    public Page<OffreEmploi> lister(boolean personnel, OffreStatut statut, Long departementId,
            String texte, com.itu.gestionRH.entity.enums.ContratType typeContrat, Pageable pageable) {
        String recherche = texte == null || texte.isBlank() ? null : texte.trim();
        return personnel
                ? offres.rechercherInterne(statut, LocalDate.now(), departementId, typeContrat, recherche, pageable)
                : offres.rechercherPubliques(LocalDate.now(), departementId, typeContrat, recherche, pageable);
    }

    public OffreEmploi obtenir(Long id, boolean personnel) {
        OffreEmploi offre = trouver(id);
        if (!personnel && (offre.getStatutOffre() != OffreStatut.PUBLIEE
                || (offre.getDateLimiteCandidature() != null && offre.getDateLimiteCandidature().isBefore(LocalDate.now())))) {
            throw new NotFoundException("Offre introuvable.");
        }
        return offre;
    }

    public long nombreCandidatures(Long offreId) {
        return candidatures.countByOffreId(offreId);
    }

    @Transactional
    public int cloturerOffresExpirees(LocalDate dateReference) {
        List<OffreEmploi> expirees = offres.findByStatutOffreAndDateLimiteCandidatureBefore(OffreStatut.PUBLIEE, dateReference);
        WorkflowContext contexte = WorkflowContext.of("SYSTEME", java.util.Set.of(),
                java.util.Set.of("OFFRE_PUBLIER"), "Clôture automatique après la date limite.");
        for (OffreEmploi offre : expirees) {
            offre.setDateCloture(dateReference);
            machine.transition(offre, "cloturer", contexte);
        }
        offres.flush();
        return expirees.size();
    }

    public List<String> actionsDisponibles(OffreEmploi offre, WorkflowContext contexte) {
        return machine.actionsDisponibles(offre, contexte);
    }

    private OffreEmploi trouver(Long id) {
        return offres.findById(id).orElseThrow(() -> new NotFoundException("Offre introuvable."));
    }

    private void exigerBrouillon(OffreEmploi offre) {
        if (offre.getStatutOffre() != OffreStatut.BROUILLON) {
            throw erreur("OFFRE_NON_MODIFIABLE", "Seule une offre brouillon peut être modifiée ou publiée.", HttpStatus.CONFLICT);
        }
    }

    private void verifierDateLimite(LocalDate date) {
        if (date != null && date.isBefore(LocalDate.now())) {
            throw erreur("DATE_LIMITE_DEPASSEE", "La date limite de candidature doit être aujourd'hui ou ultérieure.", HttpStatus.BAD_REQUEST);
        }
    }

    private static String texteOu(String valeur, String valeurDefaut) {
        return valeur == null || valeur.isBlank() ? valeurDefaut : valeur.trim();
    }

    private static String netoyer(String valeur) { return valeur == null || valeur.isBlank() ? null : valeur.trim(); }

    private static BusinessRuleException erreur(String code, String message, HttpStatus status) {
        return new BusinessRuleException(code, message, status);
    }
}
