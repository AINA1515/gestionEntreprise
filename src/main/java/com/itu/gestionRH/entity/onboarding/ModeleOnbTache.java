package com.itu.gestionRH.entity.onboarding;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.ServiceResponsable;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MODELE_ONB_TACHE")
public class ModeleOnbTache {

    protected ModeleOnbTache() {}

    public static ModeleOnbTache nouvelle() { return new ModeleOnbTache(); }

    @Column(name = "ID", precision = 10, scale = 0)
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
    @Enumerated(EnumType.STRING)
    private ServiceResponsable serviceResponsable;

    @Column(name = "DELAI_JOURS", precision = 3, scale = 0, nullable = false)
    private Integer delaiJours;

    @Column(name = "EST_OBLIGATOIRE", precision = 1, scale = 0)
    private Boolean estObligatoire;

    @Column(name = "ORDRE", precision = 3, scale = 0)
    private Integer ordre;

    @Column(name = "EST_ACTIF", nullable = false)
    private Boolean estActif;

    // Colonnes d'audit gérées par le trigger TBR_MODELE_ONB_TACHE (script GC-01)
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

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ServiceResponsable getServiceResponsable() { return serviceResponsable; }
    public void setServiceResponsable(ServiceResponsable serviceResponsable) { this.serviceResponsable = serviceResponsable; }

    public Integer getDelaiJours() { return delaiJours; }
    public void setDelaiJours(Integer delaiJours) { this.delaiJours = delaiJours; }

    public Boolean getEstObligatoire() { return estObligatoire; }
    public void setEstObligatoire(Boolean estObligatoire) { this.estObligatoire = estObligatoire; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public Boolean getEstActif() { return estActif; }
    public void setEstActif(Boolean estActif) { this.estActif = estActif; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
