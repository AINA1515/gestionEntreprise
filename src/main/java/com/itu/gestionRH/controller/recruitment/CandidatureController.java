package com.itu.gestionRH.controller.recruitment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.itu.gestionRH.entity.recruitment.Candidature;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;

/** Candidatures : depot cote candidat, suivi cote RH. */
@RestController
@RequestMapping("/api/v1")
public class CandidatureController {

    public record CandidatureResponse(Long id, String candidat, String email, String poste,
                                      LocalDate datePostulation, String statut) {}

    public record StatsCandidatures(long recues, long entretiensAOrganiser, long retenues) {}

    public record SuiviCandidaturesResponse(StatsCandidatures stats, List<CandidatureResponse> candidatures) {}

    public record DepotCandidatureRequest(Long offreId, String nom, String prenom, String email,
                                          String telephone, String lettreMotivation) {}
    public record StatutRequest(String statut, String commentaire) {}

    private final CandidatureRepository candidatureRepository;

    public CandidatureController(CandidatureRepository candidatureRepository) {
        this.candidatureRepository = candidatureRepository;
    }

    /** Ex-page /rh/candidatures. */
    @GetMapping("/recrutement/candidatures/synthese")
    @Transactional(readOnly = true)
    public SuiviCandidaturesResponse suivi() {
        List<Candidature> toutes = candidatureRepository.findAll();
        StatsCandidatures stats = new StatsCandidatures(
                toutes.size(),
                toutes.stream().filter(c -> "PRESELECTIONNEE".equals(c.getStatutCandidature())).count(),
                toutes.stream().filter(c -> "RETENUE".equals(c.getStatutCandidature())).count());
        return new SuiviCandidaturesResponse(stats, toutes.stream().map(CandidatureController::toResponse).toList());
    }

    /** Ex-page /candidat/candidature (depot d'une candidature). */
    @PostMapping("/recrutement/candidatures")
    public CandidatureResponse deposer(@RequestBody DepotCandidatureRequest request) {
        // TODO : CandidatureService.deposer(request) (+ import du CV en multipart)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Depot de candidature a implementer");
    }

    @GetMapping("/recrutement/candidatures")
    @Transactional(readOnly = true)
    public List<CandidatureResponse> lister(@RequestParam(required = false) Long offreId,
                                            @RequestParam(required = false) String statut) {
        return candidatureRepository.findAll().stream()
                .filter(c -> offreId == null || (c.getOffre() != null && offreId.equals(c.getOffre().getId())))
                .filter(c -> statut == null || statut.equals(c.getStatutCandidature()))
                .map(CandidatureController::toResponse).toList();
    }

    @PatchMapping("/recrutement/candidatures/{id}/statut")
    public CandidatureResponse changerStatut(@PathVariable Long id, @RequestBody StatutRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Modification du statut de candidature à implémenter");
    }

    private static CandidatureResponse toResponse(Candidature c) {
        var cand = c.getCandidat();
        String nom = cand == null ? null : ((cand.getPrenom() == null ? "" : cand.getPrenom() + " ")
                + (cand.getNom() == null ? "" : cand.getNom())).trim();
        return new CandidatureResponse(c.getId(), nom, cand != null ? cand.getEmail() : null,
                c.getOffre() != null ? c.getOffre().getTitre() : null,
                c.getDatePostulation(), c.getStatutCandidature());
    }
}
