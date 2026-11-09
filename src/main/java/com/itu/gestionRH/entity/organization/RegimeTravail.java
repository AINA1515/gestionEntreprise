package com.itu.gestionRH.entity.organization;
import jakarta.persistence.*; import java.math.BigDecimal; import java.util.*;
@Entity @Table(name="REGIME_TRAVAIL") public class RegimeTravail {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="regimeGen") @SequenceGenerator(name="regimeGen",sequenceName="SEQ_REGIME_TRAVAIL",allocationSize=1) private Long id;
 @Column(nullable=false,unique=true,length=30) private String code; @Column(nullable=false,length=120) private String libelle; @Column(length=500) private String description; @Column(name="HEURES_HEBDOMADAIRES") private BigDecimal heuresHebdomadaires; @Column(name="EST_ACTIF") private Integer estActif=1;
 public Long getId(){return id;} public String getCode(){return code;} public void setCode(String v){code=v;} public String getLibelle(){return libelle;} public void setLibelle(String v){libelle=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public BigDecimal getHeuresHebdomadaires(){return heuresHebdomadaires;} public void setHeuresHebdomadaires(BigDecimal v){heuresHebdomadaires=v;} public Integer getEstActif(){return estActif;} public void setEstActif(Integer v){estActif=v;}
}
