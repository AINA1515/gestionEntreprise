package com.itu.gestionRH.entity.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "DEPARTEMENT")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departementGen")
    @SequenceGenerator(name = "departementGen", sequenceName = "SEQ_DEPARTEMENT", allocationSize = 1)
    private Long id;

    @Column(name = "CODE", nullable = false, length = 20)
    private String code;

    @Column(name = "LIBELLE", nullable = false, length = 150)
    private String libelle;

    @Column(name = "RESPONSABLE_ID")
    private Long responsableId;

    @Column(name = "DEPARTEMENT_PARENT_ID")
    private Long departementParentId;

    @Column(name = "EST_ACTIF")
    private Boolean estActif;

    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    private List<Poste> postes = new ArrayList<>();

    // Historique des responsables du département (table alimentée par triggers TAH_*)
    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    private List<DepartementResponsable> historiqueResponsables = new ArrayList<>();

    @OneToMany(mappedBy = "departementParent", fetch = FetchType.LAZY)
    private List<DepartementRattachment> departementRattachements = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_DEPARTEMENT (script GC-01)
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

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Long getResponsableId() { return responsableId; }
    public void setResponsableId(Long responsableId) { this.responsableId = responsableId; }

    public Long getDepartementParentId() { return departementParentId; }
    public void setDepartementParentId(Long departementParentId) { this.departementParentId = departementParentId; }

    public Boolean getEstActif() { return estActif; }
    public void setEstActif(Boolean estActif) { this.estActif = estActif; }

    public List<Poste> getPostes() { return postes; }
    public void setPostes(List<Poste> postes) { this.postes = postes; }

    public List<DepartementResponsable> getHistoriqueResponsables() { return historiqueResponsables; }
    public void setHistoriqueResponsables(List<DepartementResponsable> historiqueResponsables) { this.historiqueResponsables = historiqueResponsables; }

    public List<DepartementRattachment> getDepartementRattachements() { return departementRattachements; }
    public void setDepartementRattachements(List<DepartementRattachment> departementRattachements) { this.departementRattachements = departementRattachements; }

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
