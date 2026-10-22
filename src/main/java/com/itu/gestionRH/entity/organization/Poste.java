package com.itu.gestionRH.entity.organization;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.PosteStatut;
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

import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.entity.hiring.Affectation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "POSTE")
public class Poste {

    protected Poste() {}

    public static Poste nouvelle() { return new Poste(); }

    @Column(name = "ID", precision = 10, scale = 0)
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
    @Enumerated(EnumType.STRING)
    private PosteStatut statutPoste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTEMENT_ID", nullable = false)
    private Departement departement;

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    private List<DemandeRecrutement> demandesRecrutements = new ArrayList<>();

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    private List<Affectation> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    private List<PosteDepartement> posteDepartements = new ArrayList<>();

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

    public String getCodePoste() { return codePoste; }
    public void setCodePoste(String codePoste) { this.codePoste = codePoste; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PosteStatut getStatutPoste() { return statutPoste; }
    public void appliquerTransitionWorkflow(PosteStatut nouvelEtat) { this.statutPoste = nouvelEtat; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    public List<DemandeRecrutement> getDemandesRecrutements() { return demandesRecrutements; }
    public void setDemandesRecrutements(List<DemandeRecrutement> demandesRecrutements) { this.demandesRecrutements = demandesRecrutements; }

    public List<Affectation> getAffectations() { return affectations; }
    public void setAffectations(List<Affectation> affectations) { this.affectations = affectations; }

    public List<PosteDepartement> getPosteDepartements() { return posteDepartements; }
    public void setPosteDepartements(List<PosteDepartement> posteDepartements) { this.posteDepartements = posteDepartements; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
