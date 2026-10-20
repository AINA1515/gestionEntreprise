package com.itu.gestionRH.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "LOG_CONNEXION")
public class LogConnexion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logConnexionGen")
    @SequenceGenerator(name = "logConnexionGen", sequenceName = "SEQ_LOG_CONNEXION", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UTILISATEUR_ID")
    private Utilisateur utilisateur;

    @Column(name = "LOGIN_SAISI", length = 100)
    private String loginSaisi;

    @Column(name = "RESULTAT", nullable = false, length = 12)
    private String resultat;

    @Column(name = "DATE_EVENEMENT", nullable = false)
    private LocalDateTime dateEvenement;

    @Column(name = "ADRESSE_IP", length = 45)
    private String adresseIp;

    @Column(name = "USER_AGENT", length = 300)
    private String userAgent;

    // Table de journal (ajout seul) : pas de colonnes d'audit en base, uniquement
    // le trigger TBR_ pour l'ID.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getLoginSaisi() {
        return loginSaisi;
    }

    public void setLoginSaisi(String loginSaisi) {
        this.loginSaisi = loginSaisi;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public LocalDateTime getDateEvenement() {
        return dateEvenement;
    }

    public void setDateEvenement(LocalDateTime dateEvenement) {
        this.dateEvenement = dateEvenement;
    }

    public String getAdresseIp() {
        return adresseIp;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
