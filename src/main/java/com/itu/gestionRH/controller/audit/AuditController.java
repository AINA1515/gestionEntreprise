package com.itu.gestionRH.controller.audit;

import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.entity.enums.AuditAction;
import com.itu.gestionRH.service.audit.AuditService;
import com.itu.gestionRH.service.audit.AuditService.AuditDto;
import com.itu.gestionRH.service.audit.AuditService.Filtre;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Consultation du journal d'audit (qui a changé quoi, quand). Les valeurs sensibles restent masquées sans habilitation. */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {
    private final AuditService service;
    public AuditController(AuditService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_AUDIT_CONSULTER')")
    public PageResponse<AuditDto> rechercher(@RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String entite, @RequestParam(required = false) Long entiteId,
            @RequestParam(required = false) String login, @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
            @PageableDefault(size = 50, sort = "dateEvenement", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(service.rechercher(new Filtre(employeId, entite, entiteId, login, action, du, au), pageable));
    }
}
