package com.itu.gestionRH.entity.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import com.itu.gestionRH.entity.security.Candidat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "EMPLOYE")
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employeGen")
    @SequenceGenerator(name = "employeGen", sequenceName = "SEQ_EMPLOYE", allocationSize = 1)
    private Long id;

    @Column(name = "MATRICULE", nullable = false, length = 20)
    private String matricule;

    @Column(name = "CIVILITE", length = 10)
    private String civilite;

    @Column(name = "NOM", nullable = false, length = 100)
    private String nom;

    @Column(name = "PRENOM", nullable = false, length = 100)
    private String prenom;

    @Column(name = "DATE_NAISSANCE")
    private LocalDate dateNaissance;

    @Column(name = "EMAIL_PROFESSIONNEL", length = 150)
    private String emailProfessionnel;

    @Column(name = "TELEPHONE", length = 30)
    private String telephone;

    @Column(name = "DATE_EMBAUCHE_INITIALE", nullable = false)
    private LocalDate dateEmbaucheInitiale;

    @Column(name = "DATE_SORTIE")
    private LocalDate dateSortie;

    @Column(name = "MOTIF_SORTIE", length = 300)
    private String motifSortie;

    @Column(name = "STATUT_ACTIVITE", nullable = false, length = 15)
    private String statutActivite;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANDIDAT_ID")
    private Candidat candidat;

    // Colonnes d'audit gérées par le trigger TBR_EMPLOYE (script GC-01)
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

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getCivilite() { return civilite; }
    public void setCivilite(String civilite) { this.civilite = civilite; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getEmailProfessionnel() { return emailProfessionnel; }
    public void setEmailProfessionnel(String emailProfessionnel) { this.emailProfessionnel = emailProfessionnel; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public LocalDate getDateEmbaucheInitiale() { return dateEmbaucheInitiale; }
    public void setDateEmbaucheInitiale(LocalDate dateEmbaucheInitiale) { this.dateEmbaucheInitiale = dateEmbaucheInitiale; }

    public LocalDate getDateSortie() { return dateSortie; }
    public void setDateSortie(LocalDate dateSortie) { this.dateSortie = dateSortie; }

    public String getMotifSortie() { return motifSortie; }
    public void setMotifSortie(String motifSortie) { this.motifSortie = motifSortie; }

    public String getStatutActivite() { return statutActivite; }
    public void setStatutActivite(String statutActivite) { this.statutActivite = statutActivite; }

    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }

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
