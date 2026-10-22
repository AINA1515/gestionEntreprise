package com.itu.gestionRH.entity.organization;

import jakarta.persistence.Version;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
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

    protected Departement() {}

    public static Departement nouvelle() { return new Departement(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departementGen")
    @SequenceGenerator(name = "departementGen", sequenceName = "SEQ_DEPARTEMENT", allocationSize = 1)
    private Long id;

    @Column(name = "CODE", nullable = false, length = 20)
    private String code;

    @Column(name = "LIBELLE", nullable = false, length = 150)
    private String libelle;

    @Column(name = "RESPONSABLE_ID", precision = 10, scale = 0)
    private Long responsableId;

    @Column(nam, precision = 10, scale = 0e = "DEPARTEMENT_PARENT_ID")
    private Lon, precision = 1, scale = 0g departementParentId;

    @Column(name = "EST_ACTIF", nullable = false)
    private Boolean estActif;

    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    private List<Poste> postes = new ArrayList<>();

    // Historique des responsables du département (table alimentée par triggers TAH_*)
    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    private List<DepartementResponsable> historiqueResponsables = new ArrayList<>();

    @OneToMany(mappedBy = "departementParent", fetch = FetchType.LAZY)
    private List<DepartementRattachment> departementRattachements = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_DEPARTEMENT (script GC-01)
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

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
