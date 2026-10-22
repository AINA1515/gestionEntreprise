package com.itu.gestionRH.controller.recruitment;

import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.controller.recruitment.dto.DemandeDtos.*;
import com.itu.gestionRH.entity.enums.DemandeStatut;
import com.itu.gestionRH.service.recruitment.DemandeRecrutementService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demandes-recrutement")
public class DemandeRecrutementController {
    private final DemandeRecrutementService service;
    public DemandeRecrutementController(DemandeRecrutementService service) { this.service=service; }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @PostMapping
    public ResponseEntity<DemandeResponse> creer(@Valid @RequestBody DemandeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @PutMapping("/{id}")
    public DemandeResponse modifier(@PathVariable Long id, @Valid @RequestBody DemandeUpdateRequest request) {
        return service.modifier(id, request);
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CONSULTER')")
    @GetMapping
    public PageResponse<DemandeResponse> lister(@RequestParam(required=false) DemandeStatut statut,
            @RequestParam(required=false) Long departementId, @RequestParam(required=false) Long managerId,
            @RequestParam(required=false) LocalDate dateDebut, @RequestParam(required=false) LocalDate dateFin,
            @RequestParam(required=false) String texte,
            @PageableDefault(size=20, sort="dateCreation", direction=Sort.Direction.DESC) Pageable pageable) {
        return service.lister(statut, departementId, managerId, dateDebut, dateFin, texte, pageable);
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CONSULTER')")
    @GetMapping("/{id}")
    public DemandeDetailResponse detail(@PathVariable Long id) { return service.obtenir(id); }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @PostMapping("/{id}/soumettre")
    public DemandeResponse soumettre(@PathVariable Long id) { return service.soumettre(id); }

    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_VERIFIER_RH','PERM_DEMANDE_APPROUVER_DIR')")
    @PatchMapping("/{id}/validation")
    public DemandeResponse valider(@PathVariable Long id, @Valid @RequestBody ValidationRequest request) {
        return service.traiterValidation(id, request);
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CREER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('PERM_DEMANDE_CONSULTER')")
    @GetMapping("/synthese")
    public SyntheseResponse synthese() { return service.synthese(); }
}
