package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.OffreStatut;
import com.itu.gestionRH.entity.enums.OffreDiffusion;
import jakarta.persistence.Version;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "OFFRE_EMPLOI")
public class OffreEmploi {

    protected OffreEmploi() {}

    public static OffreEmploi nouvelle() { return new OffreEmploi(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offreEmploiGen")
    @SequenceGenerator(name = "offreEmploiGen", sequenceName = "SEQ_OFFRE_EMPLOI", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEMANDE_RECRUTEMENT_ID", nullable = false)
    private DemandeRecrutement demande;

    @Column(name = "TITRE", nullable = false, length = 250)
    private String titre;

    @Column(name = "DESCRIPTION_POSTE", length = 4000)
    private String descriptionPoste;

    @Column(name = "PROFIL_RECHERCHE", length = 4000)
    private String profilRecherche;

    @Column(name = "DIFFUSION", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private OffreDiffusion diffusion;

    @Column(name = "DATE_PUBLICATION")
    private LocalDate datePublication;

    @Column(name = "DATE_LIMITE_CANDIDATURE")
    private LocalDate dateLimiteCandidature;

    @Column(name = "DATE_CLOTURE")
    private LocalDate dateCloture;

    @Column(name = "STATUT_OFFRE", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private OffreStatut statutOffre;

    @OneToMany(mappedBy = "offre", fetch = FetchType.LAZY)
    private List<Candidature> candidatures = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_OFFRE_EMPLOI (script GC-01)
    @Column(name = "DATE_CREATION", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "CREE_PAR", length = 100, nullable = false, insertable = false, updatable = false)
    private String creePar;

    @Column(name = "DATE_MODIFICATION", insertable = false, updatable = false)
    private LocalDateTime dateModification;

    @Column(name = "MODIFIE_PAR", length = 100, insertable = false, updatable = false)
    private String modifiePar;

    @Version
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "VERSION_LIGNE", precision = 10, scale = 0, nullable = false, insertable = false, updatable = false)
    private Long versionLigne;

    public Long getId() { return id; }

    public DemandeRecrutement getDemande() { return demande; }
    public void setDemande(DemandeRecrutement demande) { this.demande = demande; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescriptionPoste() { return descriptionPoste; }
    public void setDescriptionPoste(String descriptionPoste) { this.descriptionPoste = descriptionPoste; }

    public String getProfilRecherche() { return profilRecherche; }
    public void setProfilRecherche(String profilRecherche) { this.profilRecherche = profilRecherche; }

    public OffreDiffusion getDiffusion() { return diffusion; }
    public void setDiffusion(OffreDiffusion diffusion) { this.diffusion = diffusion; }

    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }

    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) { this.dateLimiteCandidature = dateLimiteCandidature; }

    public LocalDate getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDate dateCloture) { this.dateCloture = dateCloture; }

    public OffreStatut getStatutOffre() { return statutOffre; }
    public void appliquerTransitionWorkflow(OffreStatut nouvelEtat) { this.statutOffre = nouvelEtat; }

    public List<Candidature> getCandidatures() { return candidatures; }
    public void setCandidatures(List<Candidature> candidatures) { this.candidatures = candidatures; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
