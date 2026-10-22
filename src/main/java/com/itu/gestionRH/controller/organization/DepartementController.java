package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.controller.organization.OrganisationDtos.*;
import com.itu.gestionRH.service.organization.DepartementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departements")
public class DepartementController {
    private final DepartementService service;
    public DepartementController(DepartementService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DEMANDE_CONSULTER','PERM_CANDIDATURE_CONSULTER','PERM_CONTRAT_GERER','PERM_ONBOARDING_ADMIN','PERM_UTILISATEUR_ADMIN','PERM_REPORTING_CONSULTER')")
    public PageResponse<DepartementDto> lister(@RequestParam(required=false) Boolean actif,
            @PageableDefault(size=20, sort="libelle") Pageable pageable) { return PageResponse.from(service.lister(actif, pageable)); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DEMANDE_CONSULTER','PERM_CANDIDATURE_CONSULTER','PERM_CONTRAT_GERER','PERM_ONBOARDING_ADMIN','PERM_UTILISATEUR_ADMIN','PERM_REPORTING_CONSULTER')")
    public DepartementDto obtenir(@PathVariable Long id) { return service.obtenir(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('PERM_UTILISATEUR_ADMIN','PERM_ONBOARDING_ADMIN')")
    public DepartementDto creer(@Valid @RequestBody DepartementRequest request) { return service.creer(request); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_UTILISATEUR_ADMIN','PERM_ONBOARDING_ADMIN')")
    public DepartementDto modifier(@PathVariable Long id, @Valid @RequestBody DepartementRequest request) { return service.modifier(id, request); }

    @PatchMapping("/{id}/responsable")
    @PreAuthorize("hasAnyAuthority('PERM_UTILISATEUR_ADMIN','PERM_ONBOARDING_ADMIN')")
    public DepartementDto changerResponsable(@PathVariable Long id, @Valid @RequestBody ChangementResponsableRequest request) {
        return service.changerResponsable(id, request);
    }

    @PatchMapping("/{id}/desactivation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('PERM_UTILISATEUR_ADMIN','PERM_ONBOARDING_ADMIN')")
    public void desactiver(@PathVariable Long id) { service.desactiver(id); }
}
