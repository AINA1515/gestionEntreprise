package com.itu.gestionRH.controller.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.itu.gestionRH.security.AccessScope;

/** Espace candidat : CV (ex-pages /candidat/cv, /candidat/cv/creer). */
@RestController
@RequestMapping("/api/v1/candidat/cv")
public class CandidatController {
    private final AccessScope accessScope;
    public CandidatController(AccessScope accessScope) { this.accessScope=accessScope; }

    public record CvRequest(String nom, String prenom, String email, String telephone, String adresse,
                            String profil, String diplome, String etablissement, Integer annee,
                            String poste, String entreprise, String periode, String missions,
                            String competences, String langues) {}

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @GetMapping
    public CvRequest consulter() {
        accessScope.requireCandidatCourant();
        // TODO : CandidatService.cvDuCandidatConnecte()
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Consultation du CV a implementer");
    }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @PutMapping
    public CvRequest enregistrer(@RequestBody CvRequest request) {
        accessScope.requireCandidatCourant();
        // TODO : CandidatService.enregistrerCv(request)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Enregistrement du CV a implementer");
    }

    /** Import d'un CV existant (PDF ou DOCX). */
    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONSULTER_PERSO')")
    @PostMapping("/import")
    public void importer(@RequestPart("fichier") MultipartFile fichier) {
        accessScope.requireCandidatCourant();
        // TODO : stockage du fichier + Candidat.cvUrl
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Import du CV a implementer");
    }
}
