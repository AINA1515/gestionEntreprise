package com.itu.gestionRH.entity.organization;

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

import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.entity.hiring.Affectation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "POSTE")
public class Poste {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posteGen")
    @SequenceGenerator(name = "posteGen", sequenceName = "SEQ_POSTE", allocationSize = 1)
    private Long id;

    @Column(name = "CODE_POSTE", nullable = false, length = 30)
    private String codePoste;

    @Column(name = "INTITULE", nullable = false, length = 200)
    private String intitule;

    @Column(name = "DESCRIPTION", length = 4000)
    private String description;

    @Column(name = "STATUT_POSTE", nullable = false, length = 10)
    private String statutPoste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTEMENT_ID", nullable = false)
    private Departement departement;

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    private List<DemandeRecrutement> demandesRecrutements = new ArrayList<>();

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    private List<Affectation> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    private List<PosteDepartement> posteDepartements = new ArrayList<>();

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

    public String getCodePoste() { return codePoste; }
    public void setCodePoste(String codePoste) { this.codePoste = codePoste; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatutPoste() { return statutPoste; }
    public void setStatutPoste(String statutPoste) { this.statutPoste = statutPoste; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    public List<DemandeRecrutement> getDemandesRecrutements() { return demandesRecrutements; }
    public void setDemandesRecrutements(List<DemandeRecrutement> demandesRecrutements) { this.demandesRecrutements = demandesRecrutements; }

    public List<Affectation> getAffectations() { return affectations; }
    public void setAffectations(List<Affectation> affectations) { this.affectations = affectations; }

    public List<PosteDepartement> getPosteDepartements() { return posteDepartements; }
    public void setPosteDepartements(List<PosteDepartement> posteDepartements) { this.posteDepartements = posteDepartements; }

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
