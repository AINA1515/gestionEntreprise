package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.entity.organization.CongeSolde;
import com.itu.gestionRH.repository.organization.CongeSoldeRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/conges/soldes")
public class CongeSoldeController {
    private final CongeSoldeRepository soldes;
    private final EmployeRepository employes;

    public CongeSoldeController(CongeSoldeRepository soldes, EmployeRepository employes) {
        this.soldes = soldes;
        this.employes = employes;
    }

    public record Request(@NotNull Long employeId, @NotNull @Min(2000) @Max(2100) Integer annee,
            @DecimalMin("0.0") double joursAcquis) {}

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public java.util.List<CongeSolde> all() { return soldes.findAll(); }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_CONTRAT_GERER')")
    public CongeSolde save(@Valid @RequestBody Request request) {
        CongeSolde solde = soldes.findByEmployeIdAndAnnee(request.employeId(), request.annee())
                .orElseGet(CongeSolde::new);
        solde.setEmploye(employes.findById(request.employeId()).orElseThrow());
        solde.setAnnee(request.annee());
        solde.setJoursAcquis(request.joursAcquis());
        double report = soldes.findByEmployeIdAndAnnee(request.employeId(), request.annee() - 1)
                .map(x -> Math.max(0, x.getDisponible())).orElse(0d);
        solde.setJoursReportes(report);
        return soldes.save(solde);
    }
}
