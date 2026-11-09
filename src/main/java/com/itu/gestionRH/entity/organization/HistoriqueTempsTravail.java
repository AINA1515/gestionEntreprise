package com.itu.gestionRH.entity.organization;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="HISTORIQUE_TEMPS_TRAVAIL") public class HistoriqueTempsTravail {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="histTempsGen") @SequenceGenerator(name="histTempsGen",sequenceName="SEQ_HISTORIQUE_TEMPS",allocationSize=1) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="EMPLOYE_ID",nullable=false) private Employe employe; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PLANNING_ID") private PlanningTravail planning; @Column(nullable=false,length=20) private String action; @Column(name="ANCIENNE_VALEUR",length=4000) private String ancienneValeur; @Column(name="NOUVELLE_VALEUR",length=4000) private String nouvelleValeur; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="MODIFIE_PAR_ID",nullable=false) private Employe modifiePar; @Column(name="DATE_MODIFICATION",nullable=false) private LocalDateTime dateModification=LocalDateTime.now();
 public void setEmploye(Employe v){employe=v;} public void setPlanning(PlanningTravail v){planning=v;} public void setAction(String v){action=v;} public void setAncienneValeur(String v){ancienneValeur=v;} public void setNouvelleValeur(String v){nouvelleValeur=v;} public void setModifiePar(Employe v){modifiePar=v;}
}
