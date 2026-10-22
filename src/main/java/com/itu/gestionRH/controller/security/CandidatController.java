package com.itu.gestionRH.controller.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** Espace candidat : CV (ex-pages /candidat/cv, /candidat/cv/creer). */
@RestController
@RequestMapping("/api/v1/candidat/cv")
public class CandidatController {

    public record CvRequest(String nom, String prenom, String email, String telephone, String adresse,
                            String profil, String diplome, String etablissement, Integer annee,
                            String poste, String entreprise, String periode, String missions,
                            String competences, String langues) {}

    @GetMapping
    public CvRequest consulter() {
        // TODO : CandidatService.cvDuCandidatConnecte()
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Consultation du CV a implementer");
    }

    @PutMapping
    public CvRequest enregistrer(@RequestBody CvRequest request) {
        // TODO : CandidatService.enregistrerCv(request)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Enregistrement du CV a implementer");
    }

    /** Import d'un CV existant (PDF ou DOCX). */
    @PostMapping("/import")
    public void importer(@RequestPart("fichier") MultipartFile fichier) {
        // TODO : stockage du fichier + Candidat.cvUrl
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Import du CV a implementer");
    }
}
