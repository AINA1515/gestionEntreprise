package com.itu.gestionRH.entity.organization;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="DEMANDE_MODIFICATION_DOSSIER")
public class DemandeModificationDossier {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="dmdGen") @SequenceGenerator(name="dmdGen",sequenceName="SEQ_DEMANDE_MODIF_DOSSIER",allocationSize=1)
 private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="EMPLOYE_ID",nullable=false) private Employe employe;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="SUPERIEUR_ID",nullable=false) private Employe superieur;
 @Lob @Column(name="ANCIENNES_VALEURS",nullable=false) private String anciennesValeurs;
 @Lob @Column(name="NOUVELLES_VALEURS",nullable=false) private String nouvellesValeurs;
 @Column(name="STATUT",nullable=false) private String statut="EN_ATTENTE";
 @Column(name="MOTIF") private String motif;
 @Column(name="DATE_DEMANDE",nullable=false) private LocalDateTime dateDemande=LocalDateTime.now();
 @Column(name="DATE_DECISION") private LocalDateTime dateDecision;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="DECIDE_PAR_ID") private Employe decidePar;
 public Long getId(){return id;} public Employe getEmploye(){return employe;} public void setEmploye(Employe v){employe=v;} public Employe getSuperieur(){return superieur;} public void setSuperieur(Employe v){superieur=v;} public String getAnciennesValeurs(){return anciennesValeurs;} public void setAnciennesValeurs(String v){anciennesValeurs=v;} public String getNouvellesValeurs(){return nouvellesValeurs;} public void setNouvellesValeurs(String v){nouvellesValeurs=v;} public String getStatut(){return statut;} public void setStatut(String v){statut=v;} public String getMotif(){return motif;} public void setMotif(String v){motif=v;} public LocalDateTime getDateDemande(){return dateDemande;} public LocalDateTime getDateDecision(){return dateDecision;} public void setDateDecision(LocalDateTime v){dateDecision=v;} public Employe getDecidePar(){return decidePar;} public void setDecidePar(Employe v){decidePar=v;}
}
