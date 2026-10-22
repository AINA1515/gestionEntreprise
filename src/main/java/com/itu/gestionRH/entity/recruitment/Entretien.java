package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.EntretienStatut;
import com.itu.gestionRH.entity.enums.TypeEntretien;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.itu.gestionRH.entity.security.Utilisateur;

@Entity
@Table(name = "ENTRETIEN")
public class Entretien {

    protected Entretien() {}

    public static Entretien nouvelle() { return new Entretien(); }

    @Column(name = "ID", precision = 10, scale = 0)
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
    @Enumerated(EnumType.STRING)
    private TypeEntretien typeEntretien;

    @Column(name = "DATE_ENTRETIEN", nullable = false)
    private LocalDate dateEntretien;

    @Column(name = "LIEU", length = 200)
    private String lieu;

    @Column(name = "STATUT_ENTRETIEN", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private EntretienStatut statutEntretien;

    @OneToOne(mappedBy = "entretien", fetch = FetchType.LAZY)
    private EvaluationCandidat evaluation;

    // Colonnes d'audit gérées par le trigger TBR_ENTRETIEN (script GC-01)
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

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }

    public Utilisateur getIntervenant() { return intervenant; }
    public void setIntervenant(Utilisateur intervenant) { this.intervenant = intervenant; }

    public TypeEntretien getTypeEntretien() { return typeEntretien; }
    public void setTypeEntretien(TypeEntretien typeEntretien) { this.typeEntretien = typeEntretien; }

    public LocalDate getDateEntretien() { return dateEntretien; }
    public void setDateEntretien(LocalDate dateEntretien) { this.dateEntretien = dateEntretien; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public EntretienStatut getStatutEntretien() { return statutEntretien; }
    public void setStatutEntretien(EntretienStatut statutEntretien) { this.statutEntretien = statutEntretien; }

    public EvaluationCandidat getEvaluation() { return evaluation; }
    public void setEvaluation(EvaluationCandidat evaluation) { this.evaluation = evaluation; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
