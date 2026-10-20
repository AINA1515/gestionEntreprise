package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.itu.gestionRH.entity.security.Utilisateur;

@Entity
@Table(name = "DEMANDE_VALIDATION")
public class DemandeValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "demandeValidationGen")
    @SequenceGenerator(name = "demandeValidationGen", sequenceName = "SEQ_DEMANDE_VALIDATION", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEMANDE_ID", nullable = false)
    private DemandeRecrutement demande;

    @Column(name = "ETAPE", nullable = false, length = 10)
    private String etape;

    @Column(name = "DECISION", nullable = false, length = 10)
    private String decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VALIDEUR_ID", nullable = false)
    private Utilisateur validateur;

    @Column(name = "DATE_DECISION", nullable = false)
    private LocalDateTime dateDecision;

    @Column(name = "JUSTIFICATION", length = 1000)
    private String justification;

    // Colonnes d'audit gérées par le trigger TBR_DEMANDE_VALIDATION (script GC-01)
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

    public String getEtape() { return etape; }
    public void setEtape(String etape) { this.etape = etape; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public Utilisateur getValidateur() { return validateur; }
    public void setValidateur(Utilisateur validateur) { this.validateur = validateur; }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

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
