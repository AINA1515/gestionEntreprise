package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.repository.security.NiveauFormationRepository;
import com.itu.gestionRH.repository.security.TypeEmploiRepository;
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
    private final TypeEmploiRepository typesEmploi;

    public ReferentielController(NiveauFormationRepository niveauxFormation, TypeEmploiRepository typesEmploi) {
        this.niveauxFormation = niveauxFormation;
        this.typesEmploi = typesEmploi;
    }

    @GetMapping("/niveaux-formation")
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DEMANDE_CONSULTER','PERM_OFFRE_PUBLIER')")
    public List<OptionDto> niveauxFormation() {
        return niveauxFormation.findByEstActifTrueOrderByOrdreAsc().stream()
                .map(niveau -> new OptionDto(niveau.getCode(), niveau.getLibelle())).toList();
    }

    @GetMapping("/types-emploi")
    @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DEMANDE_CONSULTER','PERM_OFFRE_PUBLIER')")
    public List<OptionDto> typesEmploi() {
        return typesEmploi.findByEstActifTrueOrderByOrdreAsc().stream()
                .map(type -> new OptionDto(type.getCode(), type.getLibelle())).toList();
    }
}
