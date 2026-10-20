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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "EVALUATION_CRITERE")
public class EvaluationCritere {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evaluationCritereGen")
    @SequenceGenerator(name = "evaluationCritereGen", sequenceName = "SEQ_EVALUATION_CRITERE", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVALUATION_ID", nullable = false)
    private EvaluationCandidat evaluation;

    @Column(name = "CRITERE", nullable = false, length = 150)
    private String critere;

    @Column(name = "NOTE", nullable = false, precision = 4, scale = 2)
    private BigDecimal note;

    @Column(name = "POIDS")
    private Integer poids;

    @Column(name = "COMMENTAIRE", length = 500)
    private String commentaire;

    // Colonnes d'audit gérées par le trigger TBR_EVALUATION_CRITERE (script GC-01)
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

    public EvaluationCandidat getEvaluation() { return evaluation; }
    public void setEvaluation(EvaluationCandidat evaluation) { this.evaluation = evaluation; }

    public String getCritere() { return critere; }
    public void setCritere(String critere) { this.critere = critere; }

    public BigDecimal getNote() { return note; }
    public void setNote(BigDecimal note) { this.note = note; }

    public Integer getPoids() { return poids; }
    public void setPoids(Integer poids) { this.poids = poids; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

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
