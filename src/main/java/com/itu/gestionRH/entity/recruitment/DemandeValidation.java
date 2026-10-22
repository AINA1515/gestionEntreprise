package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.DecisionValidation;
import com.itu.gestionRH.entity.enums.EtapeValidation;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.itu.gestionRH.entity.security.Utilisateur;

@Entity
@Table(name = "DEMANDE_VALIDATION")
public class DemandeValidation {

    protected DemandeValidation() {}

    public static DemandeValidation nouvelle() { return new DemandeValidation(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "demandeValidationGen")
    @SequenceGenerator(name = "demandeValidationGen", sequenceName = "SEQ_DEMANDE_VALIDATION", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEMANDE_ID", nullable = false)
    private DemandeRecrutement demande;

    @Column(name = "ETAPE", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private EtapeValidation etape;

    @Column(name = "DECISION", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private DecisionValidation decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VALIDEUR_ID", nullable = false)
    private Utilisateur validateur;

    @Column(name = "DATE_DECISION", nullable = false)
    private LocalDateTime dateDecision;

    @Column(name = "JUSTIFICATION", length = 1000)
    private String justification;

    // Colonnes d'audit gérées par le trigger TBR_DEMANDE_VALIDATION (script GC-01)
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

    public EtapeValidation getEtape() { return etape; }
    public void setEtape(EtapeValidation etape) { this.etape = etape; }

    public DecisionValidation getDecision() { return decision; }
    public void setDecision(DecisionValidation decision) { this.decision = decision; }

    public Utilisateur getValidateur() { return validateur; }
    public void setValidateur(Utilisateur validateur) { this.validateur = validateur; }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
