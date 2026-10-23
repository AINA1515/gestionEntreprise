package com.itu.gestionRH.entity.security;

import jakarta.persistence.Version;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import com.itu.gestionRH.common.BooleanToIntegerConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CANDIDAT")
public class Candidat {

    protected Candidat() {}

    public static Candidat nouvelle() { return new Candidat(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidatGen")
    @SequenceGenerator(name = "candidatGen", sequenceName = "SEQ_CANDIDAT", allocationSize = 1)
    private Long id;

    @Column(name = "NOM", nullable = false, length = 100)
    private String nom;

    @Column(name = "PRENOM", nullable = false, length = 100)
    private String prenom;

    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    @Column(name = "TELEPHONE", length = 30)
    private String telephone;

    @Column(name = "ADRESSE", length = 300)
    private String adresse;

    @Column(name = "CV_URL", length = 500)
    private String cvUrl;

    @Lob
    @Column(name = "CV_CONTENU")
    private String cvContenu;

    @Column(name = "CONSENTEMENT_RGPD", precision = 1, scale = 0, nullable = false)
    @Convert(converter = BooleanToIntegerConverter.class)
    private Boolean consentementRgpd;

    @Column(name = "DATE_CONSENTEMENT")
    private LocalDate dateConsentement;

    @Column(name = "DATE_ANONYMISATION")
    private LocalDate dateAnonymisation;

    // Colonnes d'audit gérées par le trigger TBR_CANDIDAT (script GC-01)
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

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCvUrl() {
        return cvUrl;
    }

    public void setCvUrl(String cvUrl) {
        this.cvUrl = cvUrl;
    }

    public String getCvContenu() { return cvContenu; }

    public void setCvContenu(String cvContenu) { this.cvContenu = cvContenu; }

    public Boolean getConsentementRgpd() {
        return consentementRgpd;
    }

    public void setConsentementRgpd(Boolean consentementRgpd) {
        this.consentementRgpd = consentementRgpd;
    }

    public LocalDate getDateConsentement() {
        return dateConsentement;
    }

    public void setDateConsentement(LocalDate dateConsentement) {
        this.dateConsentement = dateConsentement;
    }

    public LocalDate getDateAnonymisation() {
        return dateAnonymisation;
    }

    public void setDateAnonymisation(LocalDate dateAnonymisation) {
        this.dateAnonymisation = dateAnonymisation;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public String getCreePar() {
        return creePar;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public String getModifiePar() {
        return modifiePar;
    }

    public Long getVersionLigne() {
        return versionLigne;
    }
}
