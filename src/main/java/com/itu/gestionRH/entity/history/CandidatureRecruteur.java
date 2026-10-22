package com.itu.gestionRH.entity.history;

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
import com.itu.gestionRH.entity.recruitment.Candidature;

@Entity
@Table(name = "CANDIDATURE_RECRUTEUR")
public class CandidatureRecruteur {

    protected CandidatureRecruteur() {}

    public static CandidatureRecruteur nouvelle() { return new CandidatureRecruteur(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidatureRecruteurGen")
    @SequenceGenerator(name = "candidatureRecruteurGen", sequenceName = "SEQ_CANDIDATURE_RECRUTEUR", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANDIDATURE_ID", nullable = false)
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECRUTEUR_ID", nullable = false)
    private Utilisateur recruteur;

    @Column(name = "DATE_DEBUT", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "DATE_FIN")
    private LocalDate dateFin;

    @Column(name = "MOTIF", length = 300)
    private String motif;

    // Colonnes d'audit gérées par le trigger TBR_CANDIDATURE_RECRUTEUR (script GC-01)
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

    public Utilisateur getRecruteur() { return recruteur; }
    public void setRecruteur(Utilisateur recruteur) { this.recruteur = recruteur; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
