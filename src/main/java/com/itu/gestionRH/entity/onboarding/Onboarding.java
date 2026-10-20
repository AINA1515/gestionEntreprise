package com.itu.gestionRH.entity.onboarding;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.itu.gestionRH.entity.organization.Employe;

@Entity
@Table(name = "ONBOARDING")
public class Onboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "onboardingGen")
    @SequenceGenerator(name = "onboardingGen", sequenceName = "SEQ_ONBOARDING", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYE_ID", nullable = false)
    private Employe employe;

    @Column(name = "DATE_DEBUT", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "DATE_FIN_PREVUE", nullable = false)
    private LocalDate dateFinPrevue;

    @Column(name = "DATE_FIN_REELLE")
    private LocalDate dateFinReelle;

    @Column(name = "STATUT_GLOBAL", nullable = false, length = 12)
    private String statutGlobal;

    @OneToMany(mappedBy = "onboarding", fetch = FetchType.LAZY)
    private List<OnboardingTache> onboardingTaches = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_ONBOARDING (script GC-01)
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

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }

    public LocalDate getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDate dateFinReelle) { this.dateFinReelle = dateFinReelle; }

    public String getStatutGlobal() { return statutGlobal; }
    public void setStatutGlobal(String statutGlobal) { this.statutGlobal = statutGlobal; }

    public List<OnboardingTache> getOnboardingTaches() { return onboardingTaches; }
    public void setOnboardingTaches(List<OnboardingTache> onboardingTaches) { this.onboardingTaches = onboardingTaches; }

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
