package com.itu.gestionRH.entity.recruitment;

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
    private String diffusion;

    @Column(name = "DATE_PUBLICATION")
    private LocalDate datePublication;

    @Column(name = "DATE_LIMITE_CANDIDATURE")
    private LocalDate dateLimiteCandidature;

    @Column(name = "DATE_CLOTURE")
    private LocalDate dateCloture;

    @Column(name = "STATUT_OFFRE", nullable = false, length = 10)
    private String statutOffre;

    @OneToMany(mappedBy = "offre", fetch = FetchType.LAZY)
    private List<Candidature> candidatures = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_OFFRE_EMPLOI (script GC-01)
    @Column(name = "DATE_CREATION", insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "CREE_PAR", insertable = false, updatable = false)
    private String creePar;

    @Column(name = "DATE_MODIFICATION", insertable = false, updatable = false)
    private LocalDateTime dateModification;

    @Column(name = "MODIFIE_PAR", insertable = false, updatable = false)
    private String modifiePar;

    @Column(name = "VERSION_LIGNE", insertable = false, updatable = false)
    private Long versionLigne;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DemandeRecrutement getDemande() { return demande; }
    public void setDemande(DemandeRecrutement demande) { this.demande = demande; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescriptionPoste() { return descriptionPoste; }
    public void setDescriptionPoste(String descriptionPoste) { this.descriptionPoste = descriptionPoste; }

    public String getProfilRecherche() { return profilRecherche; }
    public void setProfilRecherche(String profilRecherche) { this.profilRecherche = profilRecherche; }

    public String getDiffusion() { return diffusion; }
    public void setDiffusion(String diffusion) { this.diffusion = diffusion; }

    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }

    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) { this.dateLimiteCandidature = dateLimiteCandidature; }

    public LocalDate getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDate dateCloture) { this.dateCloture = dateCloture; }

    public String getStatutOffre() { return statutOffre; }
    public void setStatutOffre(String statutOffre) { this.statutOffre = statutOffre; }

    public List<Candidature> getCandidatures() { return candidatures; }
    public void setCandidatures(List<Candidature> candidatures) { this.candidatures = candidatures; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getCreePar() { return creePar; }
    public void setCreePar(String creePar) { this.creePar = creePar; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public String getModifiePar() { return modifiePar; }
    public void setModifiePar(String modifiePar) { this.modifiePar = modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
    public void setVersionLigne(Long versionLigne) { this.versionLigne = versionLigne; }
}
