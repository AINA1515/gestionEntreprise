package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.controller.organization.DossierDtos.*;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.documents.DossierPiecesService;
import com.itu.gestionRH.service.documents.DossierPiecesService.EtatPieces;
import com.itu.gestionRH.service.organization.DossierService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Dossier administratif du salarié. Lecture : sien (DOSSIER_CONSULTER_PERSO) ou tous (DOSSIER_CONSULTER_TOUS).
 * Les champs sensibles et la rémunération sont masqués ou refusés selon les habilitations (voir DossierService).
 */
@RestController
@RequestMapping("/api/v1/dossiers")
public class DossierController {
    private static final String LECTURE = "hasAnyAuthority('PERM_DOSSIER_CONSULTER_TOUS','PERM_DOSSIER_CONSULTER_PERSO')";

    private final DossierService service;
    private final DossierPiecesService pieces;
    private final AccessScope accessScope;

    public DossierController(DossierService service, DossierPiecesService pieces, AccessScope accessScope) {
        this.service = service;
        this.pieces = pieces;
        this.accessScope = accessScope;
    }

    @GetMapping("/moi")
    @PreAuthorize("hasAuthority('PERM_DOSSIER_CONSULTER_PERSO')")
    public DossierDto moi() { return service.moi(accessScope.currentUserId()); }

    @PutMapping("/moi/coordonnees")
    @PreAuthorize("hasAuthority('PERM_DOSSIER_MODIFIER_PERSO')")
    public DossierDto modifierMesCoordonnees(@Valid @RequestBody CoordonneesPersonnellesRequest request) {
        return service.modifierCoordonnees(accessScope.currentUserId(), request);
    }

    @PutMapping("/moi/identite")
    @PreAuthorize("hasAuthority('PERM_DOSSIER_MODIFIER_PERSO')")
    public DossierDto modifierMonIdentite(@Valid @RequestBody IdentitePersonnelleRequest request) {
        return service.modifierIdentite(accessScope.currentUserId(), request);
    }

    @GetMapping("/moi/carriere")
    @PreAuthorize("hasAuthority('PERM_DOSSIER_CONSULTER_PERSO')")
    public List<CarriereEvenement> maCarriere() {
        return service.carriere(service.employeIdDe(accessScope.currentUserId()));
    }

    @GetMapping("/moi/remuneration")
    @PreAuthorize("hasAuthority('PERM_DOSSIER_CONSULTER_PERSO')")
    public RemunerationDto maRemuneration() {
        return service.remuneration(service.employeIdDe(accessScope.currentUserId()));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize(LECTURE)
    public DossierDto obtenir(@PathVariable Long id) { return service.obtenir(id); }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('PERM_DOSSIER_MODIFIER')")
    public DossierDto modifier(@PathVariable Long id, @Valid @RequestBody DossierRhRequest request) {
        return service.modifierParRh(id, request);
    }

    @GetMapping("/{id:\\d+}/carriere")
    @PreAuthorize(LECTURE)
    public List<CarriereEvenement> carriere(@PathVariable Long id) { return service.carriere(id); }

    @GetMapping("/{id:\\d+}/remuneration")
    @PreAuthorize(LECTURE)
    public RemunerationDto remuneration(@PathVariable Long id) { return service.remuneration(id); }

    @PostMapping("/{id:\\d+}/evolutions-salaire")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERM_CONTRAT_GERER') and hasAuthority('PERM_SALAIRE_CONSULTER')")
    public PointSalaire enregistrerEvolution(@PathVariable Long id, @Valid @RequestBody EvolutionSalaireRequest request) {
        return service.enregistrerEvolution(id, request);
    }

    @GetMapping("/moi/pieces")
    @PreAuthorize("hasAuthority('PERM_DOSSIER_CONSULTER_PERSO')")
    public EtatPieces mesPieces() { return pieces.etat(service.employeIdDe(accessScope.currentUserId())); }

    @GetMapping("/{id:\\d+}/pieces")
    @PreAuthorize(LECTURE)
    public EtatPieces pieces(@PathVariable Long id) { return pieces.etat(id); }
}
