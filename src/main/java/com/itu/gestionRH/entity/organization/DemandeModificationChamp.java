package com.itu.gestionRH.entity.organization;
import jakarta.persistence.*;
@Entity @Table(name="DEMANDE_MODIFICATION_CHAMP", uniqueConstraints=@UniqueConstraint(columnNames={"DEMANDE_ID","NOM_CHAMP"}))
public class DemandeModificationChamp {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="dmchGen") @SequenceGenerator(name="dmchGen",sequenceName="SEQ_DEMANDE_MODIF_CHAMP",allocationSize=1) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="DEMANDE_ID",nullable=false) private DemandeModificationDossier demande;
 @Column(name="NOM_CHAMP",nullable=false,length=60) private String nomChamp;
 @Column(name="ANCIENNE_VALEUR",length=4000) private String ancienneValeur;
 @Column(name="NOUVELLE_VALEUR",length=4000) private String nouvelleValeur;
 public Long getId(){return id;} public DemandeModificationDossier getDemande(){return demande;} public void setDemande(DemandeModificationDossier v){demande=v;} public String getNomChamp(){return nomChamp;} public void setNomChamp(String v){nomChamp=v;} public String getAncienneValeur(){return ancienneValeur;} public void setAncienneValeur(String v){ancienneValeur=v;} public String getNouvelleValeur(){return nouvelleValeur;} public void setNouvelleValeur(String v){nouvelleValeur=v;}
}
