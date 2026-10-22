package com.itu.gestionRH.entity.misc;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "COMPTEUR_MATRICULE")
public class CompteurMatricule {

    protected CompteurMatricule() {}

    public static CompteurMatricule nouvelle() { return new CompteurMatricule(); }
    public static CompteurMatricule pourAnnee(Integer annee) {
        CompteurMatricule compteur = new CompteurMatricule();
        compteur.id = new CompteurMatriculeId(annee);
        return compteur;
    }

    @EmbeddedId
    private CompteurMatriculeId id;

    @Column(name = "DERNIER_NUMERO", precision = 6, scale = 0, nullable = false)
    private Integer dernierNumero;

    public CompteurMatriculeId getId() { return id; }

    public Integer getDernierNumero() { return dernierNumero; }
    public void setDernierNumero(Integer dernierNumero) { this.dernierNumero = dernierNumero; }

    @Embeddable
    public static class CompteurMatriculeId implements Serializable {

        private static final long serialVersionUID = 1L;

    , precision = 4, scale = 0    @Column(name = "ANNEE", nullable = false)
        private Integer annee;

        protected CompteurMatriculeId() {}
        public CompteurMatriculeId(Integer annee) { this.annee = annee; }

        public Integer getAnnee() { return annee; }
        public void setAnnee(Integer annee) { this.annee = annee; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CompteurMatricule.CompteurMatriculeId)) return false;
            CompteurMatricule.CompteurMatriculeId that = (CompteurMatricule.CompteurMatriculeId) o;
            return annee != null && annee.equals(that.annee);
        }

        @Override
        public int hashCode() {
            return annee != null ? annee.hashCode() : 0;
        }
    }
}
