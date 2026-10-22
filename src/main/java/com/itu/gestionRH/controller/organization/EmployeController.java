package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.controller.organization.OrganisationDtos.EmployeDto;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.organization.EmployeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employes")
public class EmployeController {
    private final EmployeService service;
    private final AccessScope accessScope;
    public EmployeController(EmployeService service, AccessScope accessScope) { this.service=service; this.accessScope=accessScope; }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_EMPLOYE_CONVERTIR','PERM_ONBOARDING_ADMIN','PERM_DOCUMENT_CONSULTER_TOUS','PERM_REPORTING_CONSULTER','PERM_UTILISATEUR_ADMIN')")
    public PageResponse<EmployeDto> lister(@RequestParam(required=false) Long departementId,
            @RequestParam(required=false) EmployeStatut statutActivite, @RequestParam(required=false) String recherche,
            @PageableDefault(size=20, sort="nom") Pageable pageable) {
        return PageResponse.from(service.lister(departementId, statutActivite, recherche, pageable));
    }

    @GetMapping("/moi")
    @PreAuthorize("isAuthenticated()")
    public EmployeDto moi() { return service.moi(accessScope.currentUserId()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_EMPLOYE_CONVERTIR','PERM_ONBOARDING_ADMIN','PERM_DOCUMENT_CONSULTER_TOUS','PERM_REPORTING_CONSULTER','PERM_UTILISATEUR_ADMIN','PERM_DEMANDE_CREER')")
    public EmployeDto obtenir(@PathVariable Long id) { return service.obtenir(id); }
}
