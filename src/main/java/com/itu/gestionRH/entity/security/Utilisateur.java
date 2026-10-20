package com.itu.gestionRH.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.itu.gestionRH.entity.organization.Employe;

@Entity
@Table(name = "UTILISATEUR")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "utilisateurGen")
    @SequenceGenerator(name = "utilisateurGen", sequenceName = "SEQ_UTILISATEUR", allocationSize = 1)
    private Long id;

    @Column(name = "LOGIN", nullable = false, length = 100)
    private String login;

    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    @Column(name = "NOM_AFFICHAGE", length = 200)
    private String nomAffichage;

    @Column(name = "MOT_DE_PASSE_HASH", length = 255)
    private String motDePasseHash;

    @Column(name = "TYPE_COMPTE", nullable = false, length = 10)
    private String typeCompte;

    @Column(name = "STATUT_COMPTE", nullable = false, length = 12)
    private String statutCompte;

    @Column(name = "NB_ECHECS_CONNEXION")
    private Integer nbEchecsConnexion;

    @Column(name = "DATE_DERNIERE_CONNEXION")
    private LocalDateTime dateDerniereConnexion;

    @Column(name = "DATE_EXPIRATION_MDP")
    private LocalDate dateExpirationMdp;

    // Une seule declaration de la colonne EMPLOYE_ID : la relation (pas de champ
    // Long en doublon)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYE_ID")
    private Employe employe;

    // Une seule declaration de la colonne CANDIDAT_ID : la relation (pas de champ
    // Long en doublon)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANDIDAT_ID")
    private Candidat candidat;

    // Colonnes d'audit gérées par le trigger TBR_UTILISATEUR (script GC-01)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomAffichage() {
        return nomAffichage;
    }

    public void setNomAffichage(String nomAffichage) {
        this.nomAffichage = nomAffichage;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    // Derives des relations (EMPLOYE_ID / CANDIDAT_ID ne sont mappes qu'une seule
    // fois, via @OneToOne)
    public Long getEmployeId() {
        return employe != null ? employe.getId() : null;
    }

    public Long getCandidatId() {
        return candidat != null ? candidat.getId() : null;
    }

    public String getStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(String statutCompte) {
        this.statutCompte = statutCompte;
    }

    public Integer getNbEchecsConnexion() {
        return nbEchecsConnexion;
    }

    public void setNbEchecsConnexion(Integer nbEchecsConnexion) {
        this.nbEchecsConnexion = nbEchecsConnexion;
    }

    public LocalDateTime getDateDerniereConnexion() {
        return dateDerniereConnexion;
    }

    public void setDateDerniereConnexion(LocalDateTime dateDerniereConnexion) {
        this.dateDerniereConnexion = dateDerniereConnexion;
    }

    public LocalDate getDateExpirationMdp() {
        return dateExpirationMdp;
    }

    public void setDateExpirationMdp(LocalDate dateExpirationMdp) {
        this.dateExpirationMdp = dateExpirationMdp;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public Candidat getCandidat() {
        return candidat;
    }

    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getCreePar() {
        return creePar;
    }

    public void setCreePar(String creePar) {
        this.creePar = creePar;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public String getModifiePar() {
        return modifiePar;
    }

    public void setModifiePar(String modifiePar) {
        this.modifiePar = modifiePar;
    }

    public Long getVersionLigne() {
        return versionLigne;
    }

    public void setVersionLigne(Long versionLigne) {
        this.versionLigne = versionLigne;
    }
}
