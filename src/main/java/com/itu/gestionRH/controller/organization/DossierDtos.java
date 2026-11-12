package com.itu.gestionRH.controller.organization;

import com.itu.gestionRH.controller.organization.OrganisationDtos.AffectationResume;
import com.itu.gestionRH.controller.organization.OrganisationDtos.ContratResume;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.enums.EvolutionSalaireType;
import com.itu.gestionRH.entity.enums.SituationFamiliale;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Dossier salarié. Les blocs sont les mêmes en lecture et en écriture ; en lecture, les valeurs sensibles
 * (CIN, CNaPS, coordonnées bancaires) sont masquées (« ****123 ») sans l'habilitation requise.
 */
public final class DossierDtos {
    private DossierDtos() {}

    public record Identite(@Size(max = 10) String civilite, @NotBlank @Size(max = 100) String nom,
            @NotBlank @Size(max = 100) String prenom, @Past LocalDate dateNaissance,
            @Size(max = 100) String lieuNaissance, @Size(max = 60) String nationalite,
            @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F.") String sexe,
            SituationFamiliale situationFamiliale, @Size(max = 30) String numCin, @Size(max = 30) String numCnaps) {}

    public record Coordonnees(@Email @Size(max = 150) String emailProfessionnel, @Size(max = 30) String telephone,
            @Email @Size(max = 150) String emailPersonnel, @Size(max = 30) String telephonePersonnel,
            @Size(max = 300) String adresseLigne1, @Size(max = 200) String adresseLigne2,
            @Size(max = 10) String codePostal, @Size(max = 100) String ville, @Size(max = 60) String pays) {}

    public record ContactUrgence(@Size(max = 150) String nom, @Size(max = 50) String lien,
            @Size(max = 30) String telephone) {}

    public record CoordonneesBancaires(@Size(max = 100) String banqueNom, @Size(max = 5) String codeBanque,
            @Size(max = 5) String codeGuichet, @Size(max = 11) String numeroCompte, @Size(max = 2) String cle,
            @Size(max = 34) String iban, @Size(max = 150) String titulaire) {}

    /** Ce que l'utilisateur connecté peut voir ou modifier sur ce dossier (pour adapter l'écran). */
    public record Droits(boolean donneesSensiblesVisibles, boolean salaireVisible, boolean modifiable,
            boolean coordonneesModifiables) {}

    public record DossierDto(Long id, String matricule, EmployeStatut statutActivite, LocalDate dateEmbaucheInitiale,
            LocalDate dateSortie, String motifSortie, Long version, Identite identite, Coordonnees coordonnees,
            ContactUrgence contactUrgence, CoordonneesBancaires coordonneesBancaires, Long departementId,
            String departement, AffectationResume affectationCourante, ContratResume contratCourant, Droits droits) {}

    /** Correction du dossier par la RH. Les champs sensibles sont ignorés sans DONNEES_SENSIBLES_CONSULTER. */
    public record DossierRhRequest(Long version, @NotNull @Valid Identite identite, @NotNull @Valid Coordonnees coordonnees,
            @Valid ContactUrgence contactUrgence, @Valid CoordonneesBancaires coordonneesBancaires) {}

    /** Mise à jour par le salarié de ses propres coordonnées et de son contact d'urgence. */
    public record CoordonneesPersonnellesRequest(Long version, @Email @Size(max = 150) String emailPersonnel,
            @Size(max = 30) String telephonePersonnel, @Size(max = 300) String adresseLigne1,
            @Size(max = 200) String adresseLigne2, @Size(max = 10) String codePostal, @Size(max = 100) String ville,
            @Size(max = 60) String pays, @Valid ContactUrgence contactUrgence) {}

    /** Mise à jour par le salarié de ses informations d'identité. */
    public record IdentitePersonnelleRequest(Long version, @NotNull @Valid Identite identite) {}

    public enum CarriereEvenementType {
        EMBAUCHE, CONTRAT, AVENANT, RENOUVELLEMENT, FIN_CONTRAT, AFFECTATION, MUTATION, CHANGEMENT_SALAIRE, SORTIE
    }

    /** Une ligne de la chronologie. Les montants sont null si le salaire n'est pas visible. */
    public record CarriereEvenement(LocalDate date, CarriereEvenementType type, String libelle, String detail,
            String source, Long sourceId, BigDecimal salaireAncien, BigDecimal salaireNouveau, String devise) {}

    public record PointSalaire(LocalDate dateEffet, BigDecimal salaireAncien, BigDecimal salaireNouveau, String devise,
            String origine, String typeEvolution, String motif, Long contratId, Long evolutionId) {}

    public record RemunerationDto(Long employeId, BigDecimal salaireActuel, String devise, LocalDate dateEffetActuel,
            List<PointSalaire> historique) {}

    public record EvolutionSalaireRequest(@NotNull LocalDate dateEffet,
            @NotNull @DecimalMin("0.00") BigDecimal salaireBrutNouveau, @NotNull EvolutionSalaireType typeEvolution,
            @NotBlank @Size(max = 500) String motif) {}
}
