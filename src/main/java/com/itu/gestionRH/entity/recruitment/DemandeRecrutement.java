package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.DemandeStatut;
import com.itu.gestionRH.entity.enums.TypeDemande;
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

import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.entity.security.NiveauFormation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DEMANDE_RECRUTEMENT")
public class DemandeRecrutement {

    protected DemandeRecrutement() {}

    public static DemandeRecrutement nouvelle() { return new DemandeRecrutement(); }

    public void initialiserEnBrouillon() {
        if (id != null || statutValidation != null) throw new IllegalStateException("La demande a déjà été initialisée.");
        statutValidation = DemandeStatut.BROUILLON;
    }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "demandeRecrutementGen")
    @SequenceGenerator(name = "demandeRecrutementGen", sequenceName = "SEQ_DEMANDE_RECRUTEMENT", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POSTE_ID", nullable = false)
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANAGER_ID", nullable = false)
    private Utilisateur manager;

    @Column(name = "TYPE_DEMANDE", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private TypeDemande typeDemande;

    @Column(name = "EMPLOYE_REMPLACE_ID", precision = 10, scale = 0)
    private Long employeRemplaceId;

    @Column(name = "MOTIF", nullable = false, length = 1000)
    private String motif;

    @Column(name = "EFFECTIF_DEMANDE", precision = 3, scale = 0)
    private Integer effectifDemande;

    @Column(name = "BUDGET_ALLOUE", precision = 15, scale = 2)
    private BigDecimal budgetAlloue;


    @Column(name = "DATE_SOUHAITEE", nullable = false)
    private LocalDate dateSouhaitee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NIVEAU_FORMATION_CODE")
    private NiveauFormation niveauFormation;

    @Column(name = "STATUT_VALIDATION", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DemandeStatut statutValidation;

    @OneToMany(mappedBy = "demande", fetch = FetchType.LAZY)
    private List<OffreEmploi> offresEmplois = new ArrayList<>();

    @OneToMany(mappedBy = "demande", fetch = FetchType.LAZY)
    private List<DemandeValidation> demandesValidations = new ArrayList<>();

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

    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }

    public Utilisateur getManager() { return manager; }
    public void setManager(Utilisateur manager) { this.manager = manager; }

    public TypeDemande getTypeDemande() { return typeDemande; }
    public void setTypeDemande(TypeDemande typeDemande) { this.typeDemande = typeDemande; }

    public Long getEmployeRemplaceId() { return employeRemplaceId; }
    public void setEmployeRemplaceId(Long employeRemplaceId) { this.employeRemplaceId = employeRemplaceId; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public Integer getEffectifDemande() { return effectifDemande; }
    public void setEffectifDemande(Integer effectifDemande) { this.effectifDemande = effectifDemande; }

    public BigDecimal getBudgetAlloue() { return budgetAlloue; }
    public void setBudgetAlloue(BigDecimal budgetAlloue) { this.budgetAlloue = budgetAlloue; }


    public LocalDate getDateSouhaitee() { return dateSouhaitee; }
    public void setDateSouhaitee(LocalDate dateSouhaitee) { this.dateSouhaitee = dateSouhaitee; }

    public NiveauFormation getNiveauFormation() { return niveauFormation; }
    public void setNiveauFormation(NiveauFormation niveauFormation) { this.niveauFormation = niveauFormation; }

    public DemandeStatut getStatutValidation() { return statutValidation; }
    public void appliquerTransitionWorkflow(DemandeStatut nouvelEtat) { this.statutValidation = nouvelEtat; }

    public List<OffreEmploi> getOffresEmplois() { return offresEmplois; }
    public void setOffresEmplois(List<OffreEmploi> offresEmplois) { this.offresEmplois = offresEmplois; }

    public List<DemandeValidation> getDemandesValidations() { return demandesValidations; }
    public void setDemandesValidations(List<DemandeValidation> demandesValidations) { this.demandesValidations = demandesValidations; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
