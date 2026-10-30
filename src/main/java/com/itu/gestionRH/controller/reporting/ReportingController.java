package com.itu.gestionRH.controller.reporting;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.itu.gestionRH.service.reporting.ReportingService;

@RestController
@RequestMapping("/api/v1/reporting")
public class ReportingController {
    private final ReportingService service;

    public ReportingController(ReportingService service) { this.service = service; }

    @PreAuthorize("hasAuthority('PERM_REPORTING_CONSULTER')")
    @GetMapping("/synthese")
    public ReportingService.Synthese synthese() { return service.synthese(); }
}
