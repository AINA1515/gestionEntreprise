package com.itu.gestionRH.entity.organization;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="HORAIRE_TRAVAIL") public class HoraireTravail {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="horaireGen") @SequenceGenerator(name="horaireGen",sequenceName="SEQ_HORAIRE_TRAVAIL",allocationSize=1) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="REGIME_ID",nullable=false) private RegimeTravail regime; @Column(name="JOUR_SEMAINE") private Integer jourSemaine; @Column(name="HEURE_DEBUT") private LocalDateTime heureDebut; @Column(name="HEURE_FIN") private LocalDateTime heureFin; @Column(name="PAUSE_MINUTES") private Integer pauseMinutes=0;
 public Long getId(){return id;} public RegimeTravail getRegime(){return regime;} public void setRegime(RegimeTravail v){regime=v;} public Integer getJourSemaine(){return jourSemaine;} public void setJourSemaine(Integer v){jourSemaine=v;} public LocalDateTime getHeureDebut(){return heureDebut;} public void setHeureDebut(LocalDateTime v){heureDebut=v;} public LocalDateTime getHeureFin(){return heureFin;} public void setHeureFin(LocalDateTime v){heureFin=v;} public Integer getPauseMinutes(){return pauseMinutes;} public void setPauseMinutes(Integer v){pauseMinutes=v;}
}
