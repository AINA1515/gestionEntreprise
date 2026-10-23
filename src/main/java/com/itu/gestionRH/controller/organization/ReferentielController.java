package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.repository.security.NiveauFormationRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/referentiels")
public class ReferentielController {
    public record OptionDto(String code, String libelle) {}

    private final NiveauFormationRepository niveauxFormation;

    public ReferentielController(NiveauFormationRepository niveauxFormation) {
        this.niveauxFormation = niveauxFormation;
    }

    @GetMapping("/niveaux-formation")
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DEMANDE_CONSULTER','PERM_OFFRE_PUBLIER')")
    public List<OptionDto> niveauxFormation() {
        return niveauxFormation.findByEstActifTrueOrderByOrdreAsc().stream()
                .map(niveau -> new OptionDto(niveau.getCode(), niveau.getLibelle())).toList();
    }
}
