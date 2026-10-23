package com.itu.gestionRH.controller.recruitment;

import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.controller.recruitment.dto.OffreDtos.OffreCreateRequest;
import com.itu.gestionRH.controller.recruitment.dto.OffreDtos.OffreInterneResponse;
import com.itu.gestionRH.controller.recruitment.dto.OffreDtos.OffrePubliqueResponse;
import com.itu.gestionRH.controller.recruitment.dto.OffreDtos.OffreUpdateRequest;
import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.enums.OffreStatut;
import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import com.itu.gestionRH.service.recruitment.OffreEmploiService;
import com.itu.gestionRH.workflow.WorkflowContext;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Publication et consultation des offres de recrutement. */
@RestController
@RequestMapping("/api/v1/recrutement/offres")
public class OffreEmploiController {
    private final OffreEmploiService service;

    public OffreEmploiController(OffreEmploiService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('PERM_OFFRE_PUBLIER')")
    @GetMapping("/demandes-eligibles")
    public List<OffreEmploiService.DemandeEligible> demandesEligibles() {
        return service.demandesEligibles();
    }

    @PreAuthorize("hasAuthority('PERM_OFFRE_PUBLIER')")
    @PostMapping
    public ResponseEntity<OffreInterneResponse> creer(@Valid @RequestBody OffreCreateRequest request) {
        OffreEmploi offre = service.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(interne(offre));
    }

    @PreAuthorize("hasAuthority('PERM_OFFRE_PUBLIER')")
    @PutMapping("/{id}")
    public OffreInterneResponse modifier(@PathVariable Long id, @Valid @RequestBody OffreUpdateRequest request) {
        return interne(service.modifier(id, request));
    }

    @PreAuthorize("hasAuthority('PERM_OFFRE_PUBLIER')")
    @PostMapping("/{id}/publier")
    public OffreInterneResponse publier(@PathVariable Long id) {
        service.publier(id, WorkflowContext.utilisateurCourant(null));
        return interne(service.obtenir(id, true));
    }

    @PreAuthorize("hasAuthority('PERM_OFFRE_PUBLIER')")
    @PostMapping("/{id}/cloturer")
    public OffreInterneResponse cloturer(@PathVariable Long id) {
        service.cloturer(id, WorkflowContext.utilisateurCourant(null));
        return interne(service.obtenir(id, true));
    }

    /** Public sans authentification : seul le personnel muni de OFFRE_PUBLIER voit les états internes. */
    @PreAuthorize("permitAll()")
    @GetMapping
    public PageResponse<?> lister(@RequestParam(required = false) OffreStatut statut,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) String texte,
            @RequestParam(required = false) ContratType typeContrat,
            @PageableDefault(size = 20, sort = "datePublication", direction = Sort.Direction.DESC) Pageable pageable) {
        boolean personnel = WorkflowContext.utilisateurCourant(null).permissions().contains("OFFRE_PUBLIER");
        Pageable pagination = org.springframework.data.domain.PageRequest.of(Math.max(pageable.getPageNumber(), 0),
                Math.min(100, Math.max(1, pageable.getPageSize())), pageable.getSort());
        Page<OffreEmploi> page = service.lister(personnel, statut, departementId, texte, typeContrat, pagination);
        return personnel ? PageResponse.from(page.map(this::interne)) : PageResponse.from(page.map(this::publique));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public Object detail(@PathVariable Long id) {
        boolean personnel = WorkflowContext.utilisateurCourant(null).permissions().contains("OFFRE_PUBLIER");
        OffreEmploi offre = service.obtenir(id, personnel);
        return personnel ? interne(offre) : publique(offre);
    }

    private OffrePubliqueResponse publique(OffreEmploi offre) {
        return new OffrePubliqueResponse(offre.getId(), offre.getTitre(), offre.getDescriptionPoste(),
                offre.getProfilRecherche(), departementId(offre), departement(offre), offre.getDatePublication(),
                offre.getDateLimiteCandidature(), offre.getStatutOffre(), offre.getLieu(),
                offre.getTypeContrat(), offre.getTempsTravail());
    }

    private OffreInterneResponse interne(OffreEmploi offre) {
        var demande = offre.getDemande();
        var manager = demande == null ? null : demande.getManager();
        return new OffreInterneResponse(offre.getId(), demande == null ? null : demande.getId(), offre.getTitre(),
                offre.getDescriptionPoste(), offre.getProfilRecherche(), departement(offre),
                manager == null ? null : manager.getId(), manager == null ? null : manager.getNomAffichage(),
                demande == null ? null : demande.getBudgetAlloue(), demande == null ? null : demande.getEffectifDemande(),
                service.nombreCandidatures(offre.getId()), offre.getDiffusion(), offre.getDatePublication(),
                offre.getDateLimiteCandidature(), offre.getDateCloture(), offre.getStatutOffre(), offre.getLieu(),
                offre.getTypeContrat(), offre.getTempsTravail(),
                service.actionsDisponibles(offre, WorkflowContext.utilisateurCourant(null)));
    }

    private String departement(OffreEmploi offre) {
        if (offre.getDemande() == null || offre.getDemande().getPoste() == null
                || offre.getDemande().getPoste().getDepartement() == null) return null;
        return offre.getDemande().getPoste().getDepartement().getLibelle();
    }

    private Long departementId(OffreEmploi offre) {
        if (offre.getDemande() == null || offre.getDemande().getPoste() == null
                || offre.getDemande().getPoste().getDepartement() == null) return null;
        return offre.getDemande().getPoste().getDepartement().getId();
    }
}
