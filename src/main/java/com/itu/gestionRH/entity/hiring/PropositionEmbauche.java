package com.itu.gestionRH.entity.hiring;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.PropositionStatut;
import com.itu.gestionRH.entity.enums.ContratType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.entity.recruitment.Candidature;

@Entity
@Table(name = "PROPOSITION_EMBAUCHE")
public class PropositionEmbauche {

    protected PropositionEmbauche() {}

    public static PropositionEmbauche nouvelle() { return new PropositionEmbauche(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "propositionEmbaucheGen")
    @SequenceGenerator(name = "propositionEmbaucheGen", sequenceName = "SEQ_PROPOSITION_EMBAUCHE", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANDIDATURE_ID", nullable = false)
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REDACTEUR_ID")
    private Utilisateur redacteur;

    @Column(name = "TYPE_CONTRAT_PROPOSE", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private ContratType typeContratPropose;

    @Column(name = "SALAIRE_BRUT_PROPOSE", precision = 15, scale = 2)
    private BigDecimal salaireBrutPropose;

    @Column(name = "DATE_DEBUT_SOUHAITEE")
    private LocalDate dateDebutSouhaitee;

    @Column(name = "DATE_ENVOI")
    private LocalDate dateEnvoi;

    @Column(name = "DATE_LIMITE_REPONSE")
    private LocalDate dateLimiteReponse;

    @Column(name = "DATE_REPONSE")
    private LocalDate dateReponse;

    @Column(name = "MOTIF_REFUS", length = 500)
    private String motifRefus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYE_ID")
    private Employe employe;

    @Column(name = "STATUT_PROPOSITION", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PropositionStatut statutProposition;

    // Colonnes d'audit gérées par le trigger TBR_PROPOSITION_EMBAUCHE (script GC-01)
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

    public Utilisateur getRedacteur() { return redacteur; }
    public void setRedacteur(Utilisateur redacteur) { this.redacteur = redacteur; }

    public ContratType getTypeContratPropose() { return typeContratPropose; }
    public void setTypeContratPropose(ContratType typeContratPropose) { this.typeContratPropose = typeContratPropose; }

    public BigDecimal getSalaireBrutPropose() { return salaireBrutPropose; }
    public void setSalaireBrutPropose(BigDecimal salaireBrutPropose) { this.salaireBrutPropose = salaireBrutPropose; }

    public LocalDate getDateDebutSouhaitee() { return dateDebutSouhaitee; }
    public void setDateDebutSouhaitee(LocalDate dateDebutSouhaitee) { this.dateDebutSouhaitee = dateDebutSouhaitee; }

    public LocalDate getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDate dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public LocalDate getDateLimiteReponse() { return dateLimiteReponse; }
    public void setDateLimiteReponse(LocalDate dateLimiteReponse) { this.dateLimiteReponse = dateLimiteReponse; }

    public LocalDate getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDate dateReponse) { this.dateReponse = dateReponse; }

    public String getMotifRefus() { return motifRefus; }
    public void setMotifRefus(String motifRefus) { this.motifRefus = motifRefus; }

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public PropositionStatut getStatutProposition() { return statutProposition; }
    public void setStatutProposition(PropositionStatut statutProposition) { this.statutProposition = statutProposition; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
