package com.itu.gestionRH.service.security;

import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.DocumentStatut;
import com.itu.gestionRH.entity.enums.TypeDocument;
import com.itu.gestionRH.entity.security.Candidat;
import com.itu.gestionRH.repository.documents.DocumentRepository;
import com.itu.gestionRH.repository.security.CandidatRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.documents.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CandidatService {
    private final CandidatRepository candidats;
    private final DocumentRepository documents;
    private final DocumentService documentService;
    private final AccessScope accessScope;

    public CandidatService(CandidatRepository candidats, DocumentRepository documents, DocumentService documentService, AccessScope accessScope) {
        this.candidats = candidats; this.documents = documents; this.documentService = documentService; this.accessScope = accessScope;
    }

    @Transactional
    public DocumentService.DocumentDto importerCv(MultipartFile fichier, HttpServletRequest request) {
        Long candidatId = accessScope.requireCandidatCourant();
        Candidat candidat = candidats.findById(candidatId).orElseThrow(() -> new com.itu.gestionRH.common.NotFoundException("Candidat introuvable."));
        var courant = documents.findFirstByEntiteRattachementAndEntiteIdAndTypeDocumentAndStatutDocumentOrderByNumeroVersionDesc(
                DocumentEntite.CANDIDAT, candidatId, TypeDocument.CV, DocumentStatut.ACTIF);
        DocumentService.DocumentDto dto = courant.isPresent()
                ? documentService.remplacer(courant.get().getId(), fichier, request)
                : documentService.televerser(fichier, TypeDocument.CV, DocumentEntite.CANDIDAT, candidatId, request);
        candidat.setCvUrl("/api/v1/documents/" + dto.id() + "/contenu");
        candidats.save(candidat);
        return dto;
    }
}
