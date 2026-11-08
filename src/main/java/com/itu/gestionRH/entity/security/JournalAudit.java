package com.itu.gestionRH.entity.security;

import com.itu.gestionRH.entity.enums.AuditAction;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Journal d'audit du dossier salarié (table JOURNAL_AUDIT, ajout seul : aucune modification ni suppression en base). */
@Entity
@Table(name = "JOURNAL_AUDIT")
public class JournalAudit {
    protected JournalAudit() {}
    public static JournalAudit nouvelle() { return new JournalAudit(); }

    @Column(name = "ID", precision = 15, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "journalAuditGen")
    @SequenceGenerator(name = "journalAuditGen", sequenceName = "SEQ_JOURNAL_AUDIT", allocationSize = 1)
    private Long id;

    @Column(name = "DATE_EVENEMENT", nullable = false)
    private LocalDateTime dateEvenement;

    @Column(name = "UTILISATEUR_ID", precision = 10, scale = 0)
    private Long utilisateurId;

    @Column(name = "LOGIN", nullable = false, length = 100)
    private String login;

    @Column(name = "ACTION", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(name = "ENTITE", nullable = false, length = 30)
    private String entite;

    @Column(name = "ENTITE_ID", precision = 10, scale = 0)
    private Long entiteId;

    @Column(name = "EMPLOYE_ID", precision = 10, scale = 0)
    private Long employeId;

    @Column(name = "CHAMP", length = 60)
    private String champ;

    @Column(name = "ANCIENNE_VALEUR", length = 2000)
    private String ancienneValeur;

    @Column(name = "NOUVELLE_VALEUR", length = 2000)
    private String nouvelleValeur;

    @Column(name = "EST_SENSIBLE", nullable = false)
    private Boolean estSensible;

    @Column(name = "MOTIF", length = 500)
    private String motif;

    @Column(name = "OPERATION_ID", length = 36)
    private String operationId;

    @Column(name = "ADRESSE_IP", length = 45)
    private String adresseIp;

    public Long getId() { return id; }
    public LocalDateTime getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDateTime v) { this.dateEvenement = v; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long v) { this.utilisateurId = v; }
    public String getLogin() { return login; }
    public void setLogin(String v) { this.login = v; }
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction v) { this.action = v; }
    public String getEntite() { return entite; }
    public void setEntite(String v) { this.entite = v; }
    public Long getEntiteId() { return entiteId; }
    public void setEntiteId(Long v) { this.entiteId = v; }
    public Long getEmployeId() { return employeId; }
    public void setEmployeId(Long v) { this.employeId = v; }
    public String getChamp() { return champ; }
    public void setChamp(String v) { this.champ = v; }
    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String v) { this.ancienneValeur = v; }
    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String v) { this.nouvelleValeur = v; }
    public Boolean getEstSensible() { return estSensible; }
    public void setEstSensible(Boolean v) { this.estSensible = v; }
    public String getMotif() { return motif; }
    public void setMotif(String v) { this.motif = v; }
    public String getOperationId() { return operationId; }
    public void setOperationId(String v) { this.operationId = v; }
    public String getAdresseIp() { return adresseIp; }
    public void setAdresseIp(String v) { this.adresseIp = v; }
}
