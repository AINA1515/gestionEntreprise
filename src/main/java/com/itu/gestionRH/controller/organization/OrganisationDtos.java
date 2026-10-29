package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.enums.PosteStatut;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public final class OrganisationDtos {
    private OrganisationDtos() {}
    public record DepartementRequest(@NotBlank @Size(max=20) String code, @NotBlank @Size(max=150) String libelle,
            Long responsableId, Long departementParentId, Boolean estActif) {}
    public record ResponsableDto(Long employeId, String matricule, String nom, String prenom, LocalDate dateDebut, LocalDate dateFin, String motif) {}
    public record DepartementDto(Long id, String code, String libelle, Boolean estActif, Long departementParentId,
            ResponsableDto responsableActuel, List<ResponsableDto> historiqueResponsables) {}
    public record ChangementResponsableRequest(@NotNull @Positive Long employeId, @Size(max=300) String motif) {}
    public record PosteRequest(@NotBlank @Size(max=30) String codePoste, @NotBlank @Size(max=200) String intitule,
            @Size(max=4000) String description, @NotNull @Positive Long departementId) {}
    public record PosteDto(Long id, String codePoste, String intitule, String description, Long departementId,
            String departement, PosteStatut statut, List<String> actionsDisponibles) {}
    public record ContratResume(Long id, String type, String acte, Long contratPrecedentId,
            LocalDate dateDebut, LocalDate dateFin, LocalDate dateFinEffective,
            java.math.BigDecimal salaireBrut, String devise, String statut) {}
    public record AffectationResume(Long posteId, String codePoste, String intitule, String statutPoste,
            LocalDate dateDebut, Long superieurId) {}
    public record EmployeDto(Long id, String matricule, String nom, String prenom, String emailProfessionnel,
            String telephone, EmployeStatut statutActivite, Long departementId, String departement,
            AffectationResume affectationCourante, List<ContratResume> contrats) {}
}
