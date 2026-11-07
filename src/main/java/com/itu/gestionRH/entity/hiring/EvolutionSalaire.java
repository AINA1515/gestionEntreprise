package com.itu.gestionRH.entity.hiring;

import com.itu.gestionRH.entity.enums.EvolutionSalaireType;
import com.itu.gestionRH.entity.organization.Employe;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/** Changement de salaire brut sans avenant (table EVOLUTION_SALAIRE, migration 015). */
@Entity
@Table(name = "EVOLUTION_SALAIRE")
public class EvolutionSalaire {

    protected EvolutionSalaire() {}

    public static EvolutionSalaire nouvelle() { return new EvolutionSalaire(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evolutionSalaireGen")
    @SequenceGenerator(name = "evolutionSalaireGen", sequenceName = "SEQ_EVOLUTION_SALAIRE", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYE_ID", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTRAT_ID")
    private Contrat contrat;

    @Column(name = "DATE_EFFET", nullable = false)
    private LocalDate dateEffet;

    @Column(name = "SALAIRE_BRUT_ANCIEN", precision = 15, scale = 2)
    private BigDecimal salaireBrutAncien;

    @Column(name = "SALAIRE_BRUT_NOUVEAU", nullable = false, precision = 15, scale = 2)
    private BigDecimal salaireBrutNouveau;

    @Column(name = "DEVISE", nullable = false, length = 3)
    private String devise;

    @Column(name = "TYPE_EVOLUTION", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private EvolutionSalaireType typeEvolution;

    @Column(name = "MOTIF", length = 500)
    private String motif;

    // Colonnes d'audit gérées par le trigger TBR_EVOLUTION_SALAIRE
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

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat contrat) { this.contrat = contrat; }

    public LocalDate getDateEffet() { return dateEffet; }
    public void setDateEffet(LocalDate dateEffet) { this.dateEffet = dateEffet; }

    public BigDecimal getSalaireBrutAncien() { return salaireBrutAncien; }
    public void setSalaireBrutAncien(BigDecimal salaireBrutAncien) { this.salaireBrutAncien = salaireBrutAncien; }

    public BigDecimal getSalaireBrutNouveau() { return salaireBrutNouveau; }
    public void setSalaireBrutNouveau(BigDecimal salaireBrutNouveau) { this.salaireBrutNouveau = salaireBrutNouveau; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public EvolutionSalaireType getTypeEvolution() { return typeEvolution; }
    public void setTypeEvolution(EvolutionSalaireType typeEvolution) { this.typeEvolution = typeEvolution; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getCreePar() { return creePar; }
    public LocalDateTime getDateModification() { return dateModification; }
    public String getModifiePar() { return modifiePar; }
    public Long getVersionLigne() { return versionLigne; }
}
