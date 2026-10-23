package com.itu.gestionRH.service.security;

import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.DocumentStatut;
import com.itu.gestionRH.entity.enums.TypeDocument;
import com.itu.gestionRH.entity.security.Candidat;
import com.itu.gestionRH.repository.documents.DocumentRepository;
import com.itu.gestionRH.repository.security.CandidatRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.documents.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import java.util.Locale;

@Service
public class CandidatService {
    public record CvDto(String nom, String prenom, String email, String telephone, String adresse,
                        String profil, String diplome, String etablissement, Integer annee,
                        String poste, String entreprise, String periode, String missions,
                        String competences, String langues) {}

    private final CandidatRepository candidats;
    private final DocumentRepository documents;
    private final DocumentService documentService;
    private final AccessScope accessScope;
    private final UtilisateurRepository utilisateurs;
    private final ObjectMapper objectMapper;

    public CandidatService(CandidatRepository candidats, DocumentRepository documents, DocumentService documentService,
            AccessScope accessScope, UtilisateurRepository utilisateurs, ObjectMapper objectMapper) {
        this.candidats = candidats; this.documents = documents; this.documentService = documentService; this.accessScope = accessScope;
        this.utilisateurs = utilisateurs; this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CvDto cvDuCandidatConnecte() {
        return versDto(candidatCourant());
    }

    @Transactional
    public CvDto enregistrerCv(CvDto demande) {
        if (demande == null) throw new BusinessRuleException("CV_REQUIS", "Les informations du CV sont obligatoires.");
        verifierObligatoires(demande);
        Candidat candidat = candidatCourant();
        String email = demande.email().trim().toLowerCase(Locale.ROOT);
        utilisateurs.findByCandidatId(candidat.getId()).ifPresent(utilisateur -> {
            if (!utilisateur.getEmail().equalsIgnoreCase(email) && utilisateurs.existsByEmailIgnoreCase(email))
                throw new BusinessRuleException("EMAIL_DEJA_UTILISE", "Cette adresse e-mail est déjà utilisée.");
            utilisateur.setEmail(email);
        });
        candidat.setNom(demande.nom().trim()); candidat.setPrenom(demande.prenom().trim());
        candidat.setEmail(email); candidat.setTelephone(demande.telephone().trim());
        candidat.setAdresse(texte(demande.adresse()));
        try { candidat.setCvContenu(objectMapper.writeValueAsString(demande)); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Le CV n'a pas pu être enregistré.", ex); }
        candidats.save(candidat);
        return versDto(candidat);
    }

    @Transactional
    public DocumentService.DocumentDto importerCv(MultipartFile fichier, HttpServletRequest request) {
        Long candidatId = accessScope.requireCandidatCourant();
        Candidat candidat = candidats.findById(candidatId).orElseThrow(() -> new NotFoundException("Candidat introuvable."));
        var courant = documents.findFirstByEntiteRattachementAndEntiteIdAndTypeDocumentAndStatutDocumentOrderByNumeroVersionDesc(
                DocumentEntite.CANDIDAT, candidatId, TypeDocument.CV, DocumentStatut.ACTIF);
        DocumentService.DocumentDto dto = courant.isPresent()
                ? documentService.remplacer(courant.get().getId(), fichier, request)
                : documentService.televerser(fichier, TypeDocument.CV, DocumentEntite.CANDIDAT, candidatId, request);
        candidat.setCvUrl("/api/v1/documents/" + dto.id() + "/contenu");
        candidats.save(candidat);
        return dto;
    }

    private Candidat candidatCourant() {
        Long id = accessScope.requireCandidatCourant();
        return candidats.findById(id).orElseThrow(() -> new NotFoundException("Candidat introuvable."));
    }
    private CvDto versDto(Candidat candidat) {
        if (candidat.getCvContenu() != null && !candidat.getCvContenu().isBlank()) {
            try { return objectMapper.readValue(candidat.getCvContenu(), CvDto.class); }
            catch (JsonProcessingException ignored) { /* les informations de contact restent recuperables */ }
        }
        return new CvDto(candidat.getNom(), candidat.getPrenom(), candidat.getEmail(), candidat.getTelephone(), candidat.getAdresse(),
                "", "", "", null, "", "", "", "", "", "");
    }
    private void verifierObligatoires(CvDto cv) {
        if (vide(cv.nom()) || vide(cv.prenom()) || vide(cv.email()) || vide(cv.telephone()))
            throw new BusinessRuleException("CV_CHAMPS_OBLIGATOIRES", "Le nom, le prénom, l’adresse e-mail et le téléphone sont obligatoires.");
        if (cv.nom().trim().length() > 100 || cv.prenom().trim().length() > 100 || cv.email().trim().length() > 150
                || cv.telephone().trim().length() > 30 || (cv.adresse() != null && cv.adresse().trim().length() > 300))
            throw new BusinessRuleException("CV_CHAMP_TROP_LONG", "Un des champs du CV dépasse sa longueur maximale.");
    }
    private boolean vide(String valeur) { return valeur == null || valeur.isBlank(); }
    private String texte(String valeur) { return valeur == null ? null : valeur.trim(); }
}
