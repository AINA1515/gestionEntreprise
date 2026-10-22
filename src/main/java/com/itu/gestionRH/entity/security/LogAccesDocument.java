package com.itu.gestionRH.entity.security;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.LogAccesType;
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
import com.itu.gestionRH.entity.documents.Document;

@Entity
@Table(name = "LOG_ACCES_DOCUMENT")
public class LogAccesDocument {

    protected LogAccesDocument() {}

    public static LogAccesDocument nouvelle() { return new LogAccesDocument(); }

    public void enregistrer(Document document, String login, LogAccesType type, LocalDateTime date, String ip) {
        this.document = document; this.login = login; this.typeAcces = type; this.dateAcces = date; this.adresseIp = ip;
    }

    @Column(name = "ID", precision = 15, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logAccesDocumentGen")
    @SequenceGenerator(name = "logAccesDocumentGen", sequenceName = "SEQ_LOG_ACCES_DOCUMENT", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOCUMENT_ID", nullable = false)
    private Document document;

    @Column(name = "LOGIN", nullable = false, length = 100)
    private String login;

    @Column(name = "TYPE_ACCES", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private LogAccesType typeAcces;

    @Column(name = "DATE_ACCES", nullable = false)
    private LocalDateTime dateAcces;

    @Column(name = "ADRESSE_IP", length = 45)
    private String adresseIp;

    // Table de journal (ajout seul) : pas de colonnes d'audit en base, uniquement
    // le trigger TBR_ pour l'ID.

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public LogAccesType getTypeAcces() {
        return typeAcces;
    }

    public void setTypeAcces(LogAccesType typeAcces) {
        this.typeAcces = typeAcces;
    }

    public LocalDateTime getDateAcces() {
        return dateAcces;
    }

    public void setDateAcces(LocalDateTime dateAcces) {
        this.dateAcces = dateAcces;
    }

    public String getAdresseIp() {
        return adresseIp;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
    }
}
