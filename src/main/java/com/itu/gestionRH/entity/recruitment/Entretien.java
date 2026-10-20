package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.itu.gestionRH.entity.security.Utilisateur;

@Entity
@Table(name = "ENTRETIEN")
public class Entretien {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entretienGen")
    @SequenceGenerator(name = "entretienGen", sequenceName = "SEQ_ENTRETIEN", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANDIDATURE_ID", nullable = false)
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERVENANT_ID", nullable = false)
    private Utilisateur intervenant;

    @Column(name = "TYPE_ENTRETIEN", nullable = false, length = 10)
    private String typeEntretien;

    @Column(name = "DATE_ENTRETIEN", nullable = false)
    private LocalDate dateEntretien;

    @Column(name = "LIEU", length = 200)
    private String lieu;

    @Column(name = "STATUT_ENTRETIEN", nullable = false, length = 10)
    private String statutEntretien;

    @OneToOne(mappedBy = "entretien", fetch = FetchType.LAZY)
    private EvaluationCandidat evaluation;

    // Colonnes d'audit gérées par le trigger TBR_ENTRETIEN (script GC-01)
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

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }

    public Utilisateur getIntervenant() { return intervenant; }
    public void setIntervenant(Utilisateur intervenant) { this.intervenant = intervenant; }

    public String getTypeEntretien() { return typeEntretien; }
    public void setTypeEntretien(String typeEntretien) { this.typeEntretien = typeEntretien; }

    public LocalDate getDateEntretien() { return dateEntretien; }
    public void setDateEntretien(LocalDate dateEntretien) { this.dateEntretien = dateEntretien; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getStatutEntretien() { return statutEntretien; }
    public void setStatutEntretien(String statutEntretien) { this.statutEntretien = statutEntretien; }

    public EvaluationCandidat getEvaluation() { return evaluation; }
    public void setEvaluation(EvaluationCandidat evaluation) { this.evaluation = evaluation; }

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
