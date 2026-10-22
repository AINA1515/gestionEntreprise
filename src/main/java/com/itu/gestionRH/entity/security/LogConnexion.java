package com.itu.gestionRH.entity.security;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.LogConnexionResultat;
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

    protected LogConnexion() {}

    public static LogConnexion nouvelle() { return new LogConnexion(); }

    @Column(name = "ID", precision = 15, scale = 0)
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
    @Enumerated(EnumType.STRING)
    private LogConnexionResultat resultat;

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

    public LogConnexionResultat getResultat() {
        return resultat;
    }

    public void setResultat(LogConnexionResultat resultat) {
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
