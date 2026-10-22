package com.itu.gestionRH.entity.onboarding;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.TacheStatut;
import com.itu.gestionRH.entity.enums.ServiceResponsable;
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
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.entity.history.TacheAssignment;

@Entity
@Table(name = "ONBOARDING_TACHE")
public class OnboardingTache {

    protected OnboardingTache() {}

    public static OnboardingTache nouvelle() { return new OnboardingTache(); }

    @Column(name = "ID", precision = 10, scale = 0)
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
    @Enumerated(EnumType.STRING)
    private ServiceResponsable serviceResponsable;

    @Column(name = "ASSIGNEE_A_ID", precision = 10, scale = 0)
    private Long assigneeAId;

    @Column(name = "EST_OBLIGATOIRE", precision = 1, scale = 0)
    private Boolean estObligatoire;

    @Column(name = "STATUT_TACHE", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private TacheStatut statutTache;

    @Column(name = "DATE_LIMITE")
    private LocalDate dateLimite;

    @Column(name = "DATE_REALISATION")
    private LocalDate dateRealisation;

    @Column(name = "COMMENTAIRE", length = 1000)
    private String commentaire;

    @OneToMany(mappedBy = "onboardingTache", fetch = FetchType.LAZY)
    private List<TacheAssignment> tacheAssignments = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_ONBOARDING_TACHE (script GC-01)
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

    public Onboarding getOnboarding() { return onboarding; }
    public void setOnboarding(Onboarding onboarding) { this.onboarding = onboarding; }

    public ModeleOnbTache getModele() { return modele; }
    public void setModele(ModeleOnbTache modele) { this.modele = modele; }

    public String getIntituleTache() { return intituleTache; }
    public void setIntituleTache(String intituleTache) { this.intituleTache = intituleTache; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ServiceResponsable getServiceResponsable() { return serviceResponsable; }
    public void setServiceResponsable(ServiceResponsable serviceResponsable) { this.serviceResponsable = serviceResponsable; }

    public Long getAssigneeAId() { return assigneeAId; }
    public void setAssigneeAId(Long assigneeAId) { this.assigneeAId = assigneeAId; }

    public Boolean getEstObligatoire() { return estObligatoire; }
    public void setEstObligatoire(Boolean estObligatoire) { this.estObligatoire = estObligatoire; }

    public TacheStatut getStatutTache() { return statutTache; }
    public void appliquerTransitionWorkflow(TacheStatut nouvelEtat) { this.statutTache = nouvelEtat; }

    public LocalDate getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDate dateLimite) { this.dateLimite = dateLimite; }

    public LocalDate getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDate dateRealisation) { this.dateRealisation = dateRealisation; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public List<TacheAssignment> getTacheAssignments() { return tacheAssignments; }
    public void setTacheAssignments(List<TacheAssignment> tacheAssignments) { this.tacheAssignments = tacheAssignments; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
