package com.itu.gestionRH.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.itu.gestionRH.service.security.CandidatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/** Espace candidat : CV (ex-pages /candidat/cv, /candidat/cv/creer). */
@RestController
@RequestMapping("/api/v1/candidat/cv")
public class CandidatController {
    private final CandidatService candidatService;
    public CandidatController(CandidatService candidatService) { this.candidatService=candidatService; }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @GetMapping
    public CandidatService.CvDto consulter() {
        return candidatService.cvDuCandidatConnecte();
    }

    public record CvStatutDto(boolean existe) {}

    @PreAuthorize("hasAnyAuthority('PERM_DOCUMENT_CONSULTER_PERSO','PERM_CANDIDATURE_POSTULER')")
    @GetMapping("/statut")
    public CvStatutDto statut() { return new CvStatutDto(candidatService.possedeCv()); }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @GetMapping("/niveaux-formation")
    public List<CandidatService.CvOptionDto> niveauxFormation() {
        return candidatService.niveauxFormation();
    }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @GetMapping("/types-emploi")
    public List<CandidatService.CvOptionDto> typesEmploi() {
        return candidatService.typesEmploi();
    }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @PutMapping
    public CandidatService.CvDto enregistrer(@RequestBody CandidatService.CvDto request) {
        return candidatService.enregistrerCv(request);
    }

    /** Import d'un CV existant (PDF ou DOCX). */
    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @PostMapping("/import")
    public ResponseEntity<com.itu.gestionRH.service.documents.DocumentService.DocumentDto> importer(
            @RequestPart("fichier") MultipartFile fichier, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidatService.importerCv(fichier, request));
    }
}
