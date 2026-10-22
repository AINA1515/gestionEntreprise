package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.controller.organization.OrganisationDtos.EmployeDto;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.organization.EmployeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manager/collaborateurs")
public class ManagerCollaborateurController {
    private final EmployeService employes;
    private final AccessScope accessScope;
    public ManagerCollaborateurController(EmployeService employes, AccessScope accessScope) { this.employes=employes; this.accessScope=accessScope; }
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    public PageResponse<EmployeDto> collaborateurs(@PageableDefault(size=20, sort="nom") Pageable pageable) {
        return PageResponse.from(employes.collaborateurs(accessScope.currentUserId(), pageable));
    }
}
