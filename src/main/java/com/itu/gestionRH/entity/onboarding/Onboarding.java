package com.itu.gestionRH.entity.onboarding;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.OnboardingStatut;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.itu.gestionRH.entity.organization.Employe;

@Entity
@Table(name = "ONBOARDING")
public class Onboarding {

    protected Onboarding() {}

    public static Onboarding nouvelle() { return new Onboarding(); }

    @Column(name = "ID", precision = 10, scale = 0)
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
    @Enumerated(EnumType.STRING)
    private OnboardingStatut statutGlobal;

    @OneToMany(mappedBy = "onboarding", fetch = FetchType.LAZY)
    private List<OnboardingTache> onboardingTaches = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_ONBOARDING (script GC-01)
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

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }

    public LocalDate getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDate dateFinReelle) { this.dateFinReelle = dateFinReelle; }

    public OnboardingStatut getStatutGlobal() { return statutGlobal; }
    public void appliquerTransitionWorkflow(OnboardingStatut nouvelEtat) { this.statutGlobal = nouvelEtat; }

    public List<OnboardingTache> getOnboardingTaches() { return onboardingTaches; }
    public void setOnboardingTaches(List<OnboardingTache> onboardingTaches) { this.onboardingTaches = onboardingTaches; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
