package com.itu.gestionRH.controller.recruitment;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import com.itu.gestionRH.repository.recruitment.OffreEmploiRepository;
import com.itu.gestionRH.workflow.OffreEmploiMachine;
import com.itu.gestionRH.workflow.WorkflowContext;

/** Offres d'emploi visibles par les candidats (ex-page /candidat/offres). */
@RestController
@RequestMapping("/api/v1/recrutement/offres")
public class OffreEmploiController {

    public record OffreResponse(Long id, String titre, String description, String profilRecherche,
                                String departement, LocalDate datePublication,
                                LocalDate dateLimiteCandidature, String statut, List<String> actionsDisponibles) {}
    public record OffreRequest(@NotNull Long demandeId, @NotBlank String titre, String description,
                               String profilRecherche, LocalDate dateLimiteCandidature) {}

    private final OffreEmploiRepository offreRepository;
    private final OffreEmploiMachine machine;

    public OffreEmploiController(OffreEmploiRepository offreRepository, OffreEmploiMachine machine) {
        this.offreRepository = offreRepository;
        this.machine = machine;
    }

    @PreAuthorize("hasAuthority('PERM_OFFRE_PUBLIER')")
    @PostMapping
    public OffreResponse creer(@Valid @RequestBody OffreRequest request) {
        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "Création d'offre à implémenter");
    }

    /** Par defaut, uniquement les offres PUBLIEE (?statut= pour changer). */
    @GetMapping
    @Transactional(readOnly = true)
    public List<OffreResponse> lister(@RequestParam(defaultValue = "PUBLIEE") String statut) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean peutVoirBrouillons = authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(a ->
                        a.getAuthority().equals("PERM_OFFRE_PUBLIER"));
        String statutEffectif = peutVoirBrouillons ? statut : "PUBLIEE";
        return offreRepository.findAll().stream()
                .filter(o -> statutEffectif.equalsIgnoreCase(o.getStatutOffre().name()))
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyAuthority('PERM_OFFRE_PUBLIER','PERM_CANDIDATURE_POSTULER','PERM_CANDIDATURE_CONSULTER')")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public OffreResponse detail(@PathVariable Long id) {
        return offreRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));
    }

    private OffreResponse toResponse(OffreEmploi o) {
        String departement = null;
        if (o.getDemande() != null && o.getDemande().getPoste() != null
                && o.getDemande().getPoste().getDepartement() != null) {
            departement = o.getDemande().getPoste().getDepartement().getLibelle();
        }
        return new OffreResponse(o.getId(), o.getTitre(), o.getDescriptionPoste(), o.getProfilRecherche(),
                departement, o.getDatePublication(), o.getDateLimiteCandidature(), o.getStatutOffre().name(),
                machine.actionsDisponibles(o, WorkflowContext.utilisateurCourant(null)));
    }
}
