package com.itu.gestionRH.entity.recruitment;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.CandidatureStatut;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.entity.security.Candidat;

@Entity
@Table(name = "CANDIDATURE")
public class Candidature {

    protected Candidature() {}

    public static Candidature nouvelle() { return new Candidature(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidatureGen")
    @SequenceGenerator(name = "candidatureGen", sequenceName = "SEQ_CANDIDATURE", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFRE_ID", nullable = false)
    private OffreEmploi offre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANDIDAT_ID", nullable = false)
    private Candidat candidat;

    @Column(name = "DATE_POSTULATION", nullable = false)
    private LocalDate datePostulation;

    @Column(name = "LETTRE_MOTIVATION", length = 4000)
    private String lettreMotivation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECRUTEUR_ID")
    private Utilisateur recruteur;

    @Column(name = "STATUT_CANDIDATURE", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CandidatureStatut statutCandidature;

    @Column(name = "MOTIF_REJET", length = 500)
    private String motifRejet;

    @OneToMany(mappedBy = "candidature", fetch = FetchType.LAZY)
    private List<Entretien> entretiens = new ArrayList<>();

    // Colonnes d'audit gérées par le trigger TBR_CANDIDATURE (script GC-01)
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

    public OffreEmploi getOffre() { return offre; }
    public void setOffre(OffreEmploi offre) { this.offre = offre; }

    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }

    public LocalDate getDatePostulation() { return datePostulation; }
    public void setDatePostulation(LocalDate datePostulation) { this.datePostulation = datePostulation; }

    public String getLettreMotivation() { return lettreMotivation; }
    public void setLettreMotivation(String lettreMotivation) { this.lettreMotivation = lettreMotivation; }

    public Utilisateur getRecruteur() { return recruteur; }
    public void setRecruteur(Utilisateur recruteur) { this.recruteur = recruteur; }

    public CandidatureStatut getStatutCandidature() { return statutCandidature; }
    public void setStatutCandidature(CandidatureStatut statutCandidature) { this.statutCandidature = statutCandidature; }

    public String getMotifRejet() { return motifRejet; }
    public void setMotifRejet(String motifRejet) { this.motifRejet = motifRejet; }

    public List<Entretien> getEntretiens() { return entretiens; }
    public void setEntretiens(List<Entretien> entretiens) { this.entretiens = entretiens; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
