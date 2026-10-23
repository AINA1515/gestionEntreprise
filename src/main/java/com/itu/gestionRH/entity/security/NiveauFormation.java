package com.itu.gestionRH.entity.security;

import com.itu.gestionRH.common.BooleanToIntegerConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "NIVEAU_FORMATION")
public class NiveauFormation {
    @Id
    @Column(name = "CODE", length = 30, nullable = false)
    private String code;

    @Column(name = "LIBELLE", length = 100, nullable = false)
    private String libelle;

    @Column(name = "ORDRE", nullable = false)
    private Integer ordre;

    @Column(name = "EST_ACTIF", nullable = false)
    @Convert(converter = BooleanToIntegerConverter.class)
    private Boolean estActif;

    protected NiveauFormation() {}

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public Integer getOrdre() { return ordre; }
    public Boolean getEstActif() { return estActif; }
}
