package com.itu.gestionRH.entity.notifications;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.NotificationStatut;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import com.itu.gestionRH.entity.security.Utilisateur;

@Entity
@Table(name = "NOTIFICATION")
public class Notification {

    protected Notification() {}

    public static Notification nouvelle() { return new Notification(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notificationGen")
    @SequenceGenerator(name = "notificationGen", sequenceName = "SEQ_NOTIFICATION", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESTINATAIRE_ID")
    private Utilisateur destinataire;

    @Column(name = "DESTINATAIRE_EMAIL", nullable = false, length = 150)
    private String destinataireEmail;

    @Column(name = "SUJET", nullable = false, length = 250)
    private String sujet;

    @Column(name = "CORPS", columnDefinition = "CLOB")
    private String corps;

    @Column(name = "CODE_MODELE", length = 50)
    private String codeModele;

    @Column(name = "ENTITE_RATTACHEMENT", length = 30)
    private String entiteRattachement;

    @Column(name = "ENTITE_ID", precision = 10, scale = 0)
    private Long entiteId;

    @Column(name = "STATUT_ENVOI", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private NotificationStatut statutEnvoi;

    @Co, precision = 3, scale = 0lumn(name = "NB_TENTATIVES")
    private Integer nbTentatives;

    @Column(name = "DATE_ENVOI_PREVUE", nullable = false)
    private LocalDateTime dateEnvoiPrevue;

    @Column(name = "DATE_ENVOI_REEL")
    private LocalDateTime dateEnvoiReel;

    @Column(name = "MESSAGE_ERREUR", length = 1000)
    private String messageErreur;

    // Colonnes d'audit gérées par le trigger TBR_NOTIFICATION (script GC-01)
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

    public Utilisateur getDestinataire() { return destinataire; }
    public void setDestinataire(Utilisateur destinataire) { this.destinataire = destinataire; }

    public String getDestinataireEmail() { return destinataireEmail; }
    public void setDestinataireEmail(String destinataireEmail) { this.destinataireEmail = destinataireEmail; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getCorps() { return corps; }
    public void setCorps(String corps) { this.corps = corps; }

    public String getCodeModele() { return codeModele; }
    public void setCodeModele(String codeModele) { this.codeModele = codeModele; }

    public String getEntiteRattachement() { return entiteRattachement; }
    public void setEntiteRattachement(String entiteRattachement) { this.entiteRattachement = entiteRattachement; }

    public Long getEntiteId() { return entiteId; }
    public void setEntiteId(Long entiteId) { this.entiteId = entiteId; }

    public NotificationStatut getStatutEnvoi() { return statutEnvoi; }
    public void setStatutEnvoi(NotificationStatut statutEnvoi) { this.statutEnvoi = statutEnvoi; }

    public Integer getNbTentatives() { return nbTentatives; }
    public void setNbTentatives(Integer nbTentatives) { this.nbTentatives = nbTentatives; }

    public LocalDateTime getDateEnvoiPrevue() { return dateEnvoiPrevue; }
    public void setDateEnvoiPrevue(LocalDateTime dateEnvoiPrevue) { this.dateEnvoiPrevue = dateEnvoiPrevue; }

    public LocalDateTime getDateEnvoiReel() { return dateEnvoiReel; }
    public void setDateEnvoiReel(LocalDateTime dateEnvoiReel) { this.dateEnvoiReel = dateEnvoiReel; }

    public String getMessageErreur() { return messageErreur; }
    public void setMessageErreur(String messageErreur) { this.messageErreur = messageErreur; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
