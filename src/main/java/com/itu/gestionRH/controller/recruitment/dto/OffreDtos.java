package com.itu.gestionRH.controller.recruitment.dto;

import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.enums.OffreDiffusion;
import com.itu.gestionRH.entity.enums.OffreStatut;
import com.itu.gestionRH.entity.enums.OffreTempsTravail;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class OffreDtos {
    private OffreDtos() {}

    public record OffreCreateRequest(@NotNull @Positive Long demandeId,
            @Size(max = 250) String titre, @Size(max = 4000) String description,
            @Size(max = 4000) String profilRecherche, OffreDiffusion diffusion,
            LocalDate dateLimiteCandidature, @Size(max = 200) String lieu,
            ContratType typeContrat, OffreTempsTravail tempsTravail) {}

    public record OffreUpdateRequest(@Size(max = 250) String titre,
            @Size(max = 4000) String description, @Size(max = 4000) String profilRecherche,
            OffreDiffusion diffusion, LocalDate dateLimiteCandidature,
            @Size(max = 200) String lieu, ContratType typeContrat, OffreTempsTravail tempsTravail) {}

    /** Champs diffusables publiquement, dont le salaire de référence du poste. */
    public record OffrePubliqueResponse(Long id, String titre, String description, String profilRecherche,
            Long departementId,
            String departement, LocalDate datePublication, LocalDate dateLimiteCandidature,
            OffreStatut statut, String lieu, ContratType typeContrat, OffreTempsTravail tempsTravail,
            BigDecimal salaireReference) {}

    /** Vue interne réservée au personnel habilité. */
    public record OffreInterneResponse(Long id, Long demandeId, String titre, String description,
            String profilRecherche, String departement, Long managerId, String manager,
            BigDecimal budgetDemande, Integer effectifDemande, long nombreCandidatures,
            OffreDiffusion diffusion, LocalDate datePublication, LocalDate dateLimiteCandidature,
            LocalDate dateCloture, OffreStatut statut, String lieu, ContratType typeContrat,
            OffreTempsTravail tempsTravail, List<String> actionsDisponibles) {}
}
