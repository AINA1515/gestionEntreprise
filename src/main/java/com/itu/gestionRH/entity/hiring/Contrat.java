package com.itu.gestionRH.entity.hiring;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.ContratActe;
import com.itu.gestionRH.entity.enums.ContratType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.entity.organization.Employe;

@Entity
@Table(name = "CONTRAT")
public class Contrat {

    protected Contrat() {}

    public static Contrat nouvelle() { return new Contrat(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contratGen")
    @SequenceGenerator(name = "contratGen", sequenceName = "SEQ_CONTRAT", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYE_ID", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POSTE_ID", nullable = false)
    private Poste poste;

    @Column(name = "TYPE_CONTRAT", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private ContratType typeContrat;

    @Column(name = "TYPE_ACTE", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ContratActe typeActe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTRAT_PRECEDENT_ID")
    private Contrat contratPrecedent;

    @Column(name = "DATE_DEBUT", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "DATE_FIN")
    private LocalDate dateFin;

    @Column(name = "DATE_FIN_EFFECTIVE")
    private LocalDate dateFinEffective;

    @Column(name = "SALAIRE_BRUT", nullable = false, precision = 15, scale = 2)
    private BigDecimal salaireBrut;

    @Column(name = "DEVISE", nullable = false, length = 3)
    private String devise;

    @Column(name = "STATUT_CONTRAT", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private ContratStatut statutContrat;

    @OneToMany(mappedBy = "contrat", fetch = FetchType.LAZY)
    private List<Affectation> affectations = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_CONTRAT (script GC-01)
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

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }

    public ContratType getTypeContrat() { return typeContrat; }
    public void setTypeContrat(ContratType typeContrat) { this.typeContrat = typeContrat; }

    public ContratActe getTypeActe() { return typeActe; }
    public void setTypeActe(ContratActe typeActe) { this.typeActe = typeActe; }

    public Contrat getContratPrecedent() { return contratPrecedent; }
    public void setContratPrecedent(Contrat contratPrecedent) { this.contratPrecedent = contratPrecedent; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public LocalDate getDateFinEffective() { return dateFinEffective; }
    public void setDateFinEffective(LocalDate dateFinEffective) { this.dateFinEffective = dateFinEffective; }

    public BigDecimal getSalaireBrut() { return salaireBrut; }
    public void setSalaireBrut(BigDecimal salaireBrut) { this.salaireBrut = salaireBrut; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public ContratStatut getStatutContrat() { return statutContrat; }
    public void setStatutContrat(ContratStatut statutContrat) { this.statutContrat = statutContrat; }

    public List<Affectation> getAffectations() { return affectations; }
    public void setAffectations(List<Affectation> affectations) { this.affectations = affectations; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
