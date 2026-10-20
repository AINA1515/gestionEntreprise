package com.itu.gestionRH.entity.recruitment;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DEMANDE_RECRUTEMENT")
public class DemandeRecrutement {

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
    private String typeDemande;

    @Column(name = "EMPLOYE_REMPLACE_ID")
    private Long employeRemplaceId;

    @Column(name = "MOTIF", nullable = false, length = 1000)
    private String motif;

    @Column(name = "EFFECTIF_DEMANDE")
    private Integer effectifDemande;

    @Column(name = "BUDGET_ALLOUE", precision = 15, scale = 2)
    private BigDecimal budgetAlloue;

    @Column(name = "DATE_SOUHAITEE", nullable = false)
    private LocalDate dateSouhaitee;

    @Column(name = "STATUT_VALIDATION", nullable = false, length = 20)
    private String statutValidation;

    @OneToMany(mappedBy = "demande", fetch = FetchType.LAZY)
    private List<OffreEmploi> offresEmplois = new ArrayList<>();

    @OneToMany(mappedBy = "demande", fetch = FetchType.LAZY)
    private List<DemandeValidation> demandesValidations = new ArrayList<>();

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

    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }

    public Utilisateur getManager() { return manager; }
    public void setManager(Utilisateur manager) { this.manager = manager; }

    public String getTypeDemande() { return typeDemande; }
    public void setTypeDemande(String typeDemande) { this.typeDemande = typeDemande; }

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

    public String getStatutValidation() { return statutValidation; }
    public void setStatutValidation(String statutValidation) { this.statutValidation = statutValidation; }

    public List<OffreEmploi> getOffresEmplois() { return offresEmplois; }
    public void setOffresEmplois(List<OffreEmploi> offresEmplois) { this.offresEmplois = offresEmplois; }

    public List<DemandeValidation> getDemandesValidations() { return demandesValidations; }
    public void setDemandesValidations(List<DemandeValidation> demandesValidations) { this.demandesValidations = demandesValidations; }

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
