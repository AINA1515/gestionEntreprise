package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "EVALUATION_CANDIDAT")
public class EvaluationCandidat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evaluationCandidatGen")
    @SequenceGenerator(name = "evaluationCandidatGen", sequenceName = "SEQ_EVALUATION_CANDIDAT", allocationSize = 1)
    private Long id;

    // Cote proprietaire de la relation 1-1 avec Entretien.evaluation (mappedBy = "entretien").
    // Une evaluation par entretien : contrainte UK_EVAL_ENTRETIEN en base.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENTRETIEN_ID", nullable = false)
    private Entretien entretien;

    @Column(name = "NOTE_TECHNIQUE", precision = 4, scale = 2)
    private BigDecimal noteTechnique;

    @Column(name = "COMMENTAIRES", length = 4000)
    private String commentaires;

    @Column(name = "DECISION", nullable = false, length = 12)
    private String decision;

    @OneToMany(mappedBy = "evaluation", fetch = FetchType.LAZY)
    private List<EvaluationCritere> criteres = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_EVALUATION_CANDIDAT (script GC-01)
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

    public Entretien getEntretien() { return entretien; }
    public void setEntretien(Entretien entretien) { this.entretien = entretien; }

    public BigDecimal getNoteTechnique() { return noteTechnique; }
    public void setNoteTechnique(BigDecimal noteTechnique) { this.noteTechnique = noteTechnique; }

    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public List<EvaluationCritere> getCriteres() { return criteres; }
    public void setCriteres(List<EvaluationCritere> criteres) { this.criteres = criteres; }

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
