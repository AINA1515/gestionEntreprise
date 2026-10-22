package com.itu.gestionRH.entity.security;

import jakarta.persistence.Version;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ROLE_RH")
public class RoleRh {

    protected RoleRh() {}

    public static RoleRh nouvelle() { return new RoleRh(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roleRhGen")
    @SequenceGenerator(name = "roleRhGen", sequenceName = "SEQ_ROLE_RH", allocationSize = 1)
    private Long id;

    @Column(name = "CODE", nullable = false, length = 30)
    private String code;

    @Column(name = "LIBELLE", nullable = false, length = 100)
    private String libelle;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "EST_ACTIF", precision = 1, scale = 0, nullable = false)
    private Boolean estActif;

    // Colonnes d'audit gérées par le trigger TBR_ROLE_RH (script GC-01)
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

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEstActif() {
        return estActif;
    }

    public void setEstActif(Boolean estActif) {
        this.estActif = estActif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public String getCreePar() {
        return creePar;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public String getModifiePar() {
        return modifiePar;
    }

    public Long getVersionLigne() {
        return versionLigne;
    }
}
