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
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.entity.history.TacheAssignment;

@Entity
@Table(name = "ONBOARDING_TACHE")
public class OnboardingTache {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "onboardingTacheGen")
    @SequenceGenerator(name = "onboardingTacheGen", sequenceName = "SEQ_ONBOARDING_TACHE", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ONBOARDING_ID", nullable = false)
    private Onboarding onboarding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODELE_ID")
    private ModeleOnbTache modele;

    @Column(name = "INTITULE_TACHE", nullable = false, length = 250)
    private String intituleTache;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "SERVICE_RESPONSABLE", nullable = false, length = 20)
    private String serviceResponsable;

    @Column(name = "ASSIGNEE_A_ID")
    private Long assigneeAId;

    @Column(name = "EST_OBLIGATOIRE")
    private Boolean estObligatoire;

    @Column(name = "STATUT_TACHE", nullable = false, length = 10)
    private String statutTache;

    @Column(name = "DATE_LIMITE")
    private LocalDate dateLimite;

    @Column(name = "DATE_REALISATION")
    private LocalDate dateRealisation;

    @Column(name = "COMMENTAIRE", length = 1000)
    private String commentaire;

    @OneToMany(mappedBy = "onboardingTache", fetch = FetchType.LAZY)
    private List<TacheAssignment> tacheAssignments = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_ONBOARDING_TACHE (script GC-01)
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

    public Onboarding getOnboarding() { return onboarding; }
    public void setOnboarding(Onboarding onboarding) { this.onboarding = onboarding; }

    public ModeleOnbTache getModele() { return modele; }
    public void setModele(ModeleOnbTache modele) { this.modele = modele; }

    public String getIntituleTache() { return intituleTache; }
    public void setIntituleTache(String intituleTache) { this.intituleTache = intituleTache; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getServiceResponsable() { return serviceResponsable; }
    public void setServiceResponsable(String serviceResponsable) { this.serviceResponsable = serviceResponsable; }

    public Long getAssigneeAId() { return assigneeAId; }
    public void setAssigneeAId(Long assigneeAId) { this.assigneeAId = assigneeAId; }

    public Boolean getEstObligatoire() { return estObligatoire; }
    public void setEstObligatoire(Boolean estObligatoire) { this.estObligatoire = estObligatoire; }

    public String getStatutTache() { return statutTache; }
    public void setStatutTache(String statutTache) { this.statutTache = statutTache; }

    public LocalDate getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDate dateLimite) { this.dateLimite = dateLimite; }

    public LocalDate getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDate dateRealisation) { this.dateRealisation = dateRealisation; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public List<TacheAssignment> getTacheAssignments() { return tacheAssignments; }
    public void setTacheAssignments(List<TacheAssignment> tacheAssignments) { this.tacheAssignments = tacheAssignments; }

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
