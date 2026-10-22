package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.EvaluationDecision;
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

    protected EvaluationCandidat() {}

    public static EvaluationCandidat nouvelle() { return new EvaluationCandidat(); }

    @Column(name = "ID", precision = 10, scale = 0)
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
    @Enumerated(EnumType.STRING)
    private EvaluationDecision decision;

    @OneToMany(mappedBy = "evaluation", fetch = FetchType.LAZY)
    private List<EvaluationCritere> criteres = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_EVALUATION_CANDIDAT (script GC-01)
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

    public Entretien getEntretien() { return entretien; }
    public void setEntretien(Entretien entretien) { this.entretien = entretien; }

    public BigDecimal getNoteTechnique() { return noteTechnique; }
    public void setNoteTechnique(BigDecimal noteTechnique) { this.noteTechnique = noteTechnique; }

    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }

    public EvaluationDecision getDecision() { return decision; }
    public void setDecision(EvaluationDecision decision) { this.decision = decision; }

    public List<EvaluationCritere> getCriteres() { return criteres; }
    public void setCriteres(List<EvaluationCritere> criteres) { this.criteres = criteres; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
