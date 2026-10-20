package com.itu.gestionRH.entity.onboarding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MODELE_ONB_TACHE")
public class ModeleOnbTache {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modeleOnbTacheGen")
    @SequenceGenerator(name = "modeleOnbTacheGen", sequenceName = "SEQ_MODELE_ONB_TACHE", allocationSize = 1)
    private Long id;

    @Column(name = "CODE", nullable = false, length = 40)
    private String code;

    @Column(name = "INTITULE", nullable = false, length = 250)
    private String intitule;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "SERVICE_RESPONSABLE", nullable = false, length = 20)
    private String serviceResponsable;

    @Column(name = "DELAI_JOURS")
    private Integer delaiJours;

    @Column(name = "EST_OBLIGATOIRE")
    private Boolean estObligatoire;

    @Column(name = "ORDRE")
    private Integer ordre;

    @Column(name = "EST_ACTIF")
    private Boolean estActif;

    // Colonnes d'audit gérées par le trigger TBR_MODELE_ONB_TACHE (script GC-01)
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

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getServiceResponsable() { return serviceResponsable; }
    public void setServiceResponsable(String serviceResponsable) { this.serviceResponsable = serviceResponsable; }

    public Integer getDelaiJours() { return delaiJours; }
    public void setDelaiJours(Integer delaiJours) { this.delaiJours = delaiJours; }

    public Boolean getEstObligatoire() { return estObligatoire; }
    public void setEstObligatoire(Boolean estObligatoire) { this.estObligatoire = estObligatoire; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public Boolean getEstActif() { return estActif; }
    public void setEstActif(Boolean estActif) { this.estActif = estActif; }

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
