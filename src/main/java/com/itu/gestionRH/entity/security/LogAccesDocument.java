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
import com.itu.gestionRH.entity.documents.Document;

@Entity
@Table(name = "LOG_ACCES_DOCUMENT")
public class LogAccesDocument {

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
    private String typeAcces;

    @Column(name = "DATE_ACCES", nullable = false)
    private LocalDateTime dateAcces;

    @Column(name = "ADRESSE_IP", length = 45)
    private String adresseIp;

    // Table de journal (ajout seul) : pas de colonnes d'audit en base, uniquement
    // le trigger TBR_ pour l'ID.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTypeAcces() {
        return typeAcces;
    }

    public void setTypeAcces(String typeAcces) {
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
