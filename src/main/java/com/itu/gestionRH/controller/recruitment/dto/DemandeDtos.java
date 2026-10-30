package com.itu.gestionRH.controller.recruitment.dto;

import com.itu.gestionRH.entity.enums.DemandeStatut;
import com.itu.gestionRH.entity.enums.TypeDemande;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class DemandeDtos {
    private DemandeDtos() {}

    public record DemandeCreateRequest(@NotNull @Positive Long posteId,
            @NotNull TypeDemande typeDemande, @Positive Long employeRemplaceId,
            @NotNull @Positive @Max(999) Integer effectif, @NotBlank @Size(max=1000) String motif,
            @DecimalMin("0.00") @Digits(integer=13, fraction=2) BigDecimal budget,
            @NotNull LocalDate dateSouhaitee, String niveauFormation, String typeEmploi, Boolean soumettreDirectement) {}

    public record DemandeUpdateRequest(@NotNull @Positive Long posteId,
            @NotNull TypeDemande typeDemande, @Positive Long employeRemplaceId,
            @NotNull @Positive @Max(999) Integer effectif, @NotBlank @Size(max=1000) String motif,
            @DecimalMin("0.00") @Digits(integer=13, fraction=2) BigDecimal budget,
            @NotNull LocalDate dateSouhaitee, String niveauFormation, String typeEmploi) {}

    public record ValidationRequest(@NotBlank @Pattern(regexp="TRANSMETTRE_DIRECTION|VALIDER|REFUSER|RENVOYER_MANAGER") String action,
            @Size(max=1000) String justification) {}

    public record DemandeResponse(Long id, Long posteId, String poste, Long departementId, String departement,
            Long managerId, String manager, TypeDemande typeDemande, Long employeRemplaceId,
            Integer effectif, String motif, BigDecimal budget, LocalDate dateSouhaitee,
            DemandeStatut statut, LocalDateTime dateDemande, String niveauFormation, String niveauFormationLibelle,
            String typeEmploi, String typeEmploiLibelle, List<String> actionsDisponibles) {}

    public record ValidationDto(Long id, String etape, String decision, Long valideurId, String valideur,
            LocalDateTime dateDecision, String justification) {}
    public record OffreDto(Long id, String titre, String statut, LocalDate datePublication) {}
    public record DemandeDetailResponse(DemandeResponse demande, List<ValidationDto> historiqueValidations,
            List<OffreDto> offresLiees, List<String> actionsDisponibles) {}
    public record SyntheseResponse(long nombreDemandes, List<DemandeResponse> recentes, Map<String, Long> parStatut) {}
}
