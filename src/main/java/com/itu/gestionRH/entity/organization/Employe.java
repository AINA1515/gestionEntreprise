package com.itu.gestionRH.entity.organization;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.enums.SituationFamiliale;
import jakarta.persistence.Version;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import com.itu.gestionRH.entity.security.Candidat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "EMPLOYE")
public class Employe {

    protected Employe() {}

    public static Employe nouvelle() { return new Employe(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employeGen")
    @SequenceGenerator(name = "employeGen", sequenceName = "SEQ_EMPLOYE", allocationSize = 1)
    private Long id;

    @Column(name = "MATRICULE", nullable = false, length = 20)
    private String matricule;

    @Column(name = "CIVILITE", length = 10)
    private String civilite;

    @Column(name = "NOM", nullable = false, length = 100)
    private String nom;

    @Column(name = "PRENOM", nullable = false, length = 100)
    private String prenom;

    @Column(name = "DATE_NAISSANCE")
    private LocalDate dateNaissance;

    @Column(name = "EMAIL_PROFESSIONNEL", length = 150)
    private String emailProfessionnel;

    @Column(name = "TELEPHONE", length = 30)
    private String telephone;

    @Column(name = "DATE_EMBAUCHE_INITIALE", nullable = false)
    private LocalDate dateEmbaucheInitiale;

    @Column(name = "DATE_SORTIE")
    private LocalDate dateSortie;

    @Column(name = "MOTIF_SORTIE", length = 300)
    private String motifSortie;

    @Column(name = "STATUT_ACTIVITE", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private EmployeStatut statutActivite;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANDIDAT_ID")
    private Candidat candidat;

    // --- Bloc 5 : dossier administratif (migration 015) ---
    @Column(name = "LIEU_NAISSANCE", length = 100)
    private String lieuNaissance;

    @Column(name = "NATIONALITE", length = 60)
    private String nationalite;

    @Column(name = "SEXE", length = 1)
    private String sexe;

    @Column(name = "NUM_CIN", length = 30)
    private String numCin;

    @Column(name = "NUM_CNAPS", length = 30)
    private String numCnaps;

    @Column(name = "EMAIL_PERSONNEL", length = 150)
    private String emailPersonnel;

    @Column(name = "TELEPHONE_PERSONNEL", length = 30)
    private String telephonePersonnel;

    @Column(name = "ADRESSE_LIGNE1", length = 300)
    private String adresseLigne1;

    @Column(name = "ADRESSE_LIGNE2", length = 200)
    private String adresseLigne2;

    @Column(name = "CODE_POSTAL", length = 10)
    private String codePostal;

    @Column(name = "VILLE", length = 100)
    private String ville;

    @Column(name = "PAYS", length = 60)
    private String pays;

    @Column(name = "URGENCE_NOM", length = 150)
    private String urgenceNom;

    @Column(name = "URGENCE_LIEN", length = 50)
    private String urgenceLien;

    @Column(name = "URGENCE_TELEPHONE", length = 30)
    private String urgenceTelephone;

    @Column(name = "BANQUE_NOM", length = 100)
    private String banqueNom;

    @Column(name = "RIB_CODE_BANQUE", length = 5)
    private String ribCodeBanque;

    @Column(name = "RIB_CODE_GUICHET", length = 5)
    private String ribCodeGuichet;

    @Column(name = "RIB_NUMERO_COMPTE", length = 11)
    private String ribNumeroCompte;

    @Column(name = "RIB_CLE", length = 2)
    private String ribCle;

    @Column(name = "IBAN", length = 34)
    private String iban;

    @Column(name = "TITULAIRE_COMPTE", length = 150)
    private String titulaireCompte;

    @Column(name = "SITUATION_FAMILIALE", length = 15)
    @Enumerated(EnumType.STRING)
    private SituationFamiliale situationFamiliale;

    // Colonnes d'audit gérées par le trigger TBR_EMPLOYE (script GC-01)
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

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getCivilite() { return civilite; }
    public void setCivilite(String civilite) { this.civilite = civilite; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getEmailProfessionnel() { return emailProfessionnel; }
    public void setEmailProfessionnel(String emailProfessionnel) { this.emailProfessionnel = emailProfessionnel; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public LocalDate getDateEmbaucheInitiale() { return dateEmbaucheInitiale; }
    public void setDateEmbaucheInitiale(LocalDate dateEmbaucheInitiale) { this.dateEmbaucheInitiale = dateEmbaucheInitiale; }

    public LocalDate getDateSortie() { return dateSortie; }
    public void setDateSortie(LocalDate dateSortie) { this.dateSortie = dateSortie; }

    public String getMotifSortie() { return motifSortie; }
    public void setMotifSortie(String motifSortie) { this.motifSortie = motifSortie; }

    public EmployeStatut getStatutActivite() { return statutActivite; }
    public void setStatutActivite(EmployeStatut statutActivite) { this.statutActivite = statutActivite; }

    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }

    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }

    public String getNationalite() { return nationalite; }
    public void setNationalite(String nationalite) { this.nationalite = nationalite; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public String getNumCin() { return numCin; }
    public void setNumCin(String numCin) { this.numCin = numCin; }

    public String getNumCnaps() { return numCnaps; }
    public void setNumCnaps(String numCnaps) { this.numCnaps = numCnaps; }

    public String getEmailPersonnel() { return emailPersonnel; }
    public void setEmailPersonnel(String emailPersonnel) { this.emailPersonnel = emailPersonnel; }

    public String getTelephonePersonnel() { return telephonePersonnel; }
    public void setTelephonePersonnel(String telephonePersonnel) { this.telephonePersonnel = telephonePersonnel; }

    public String getAdresseLigne1() { return adresseLigne1; }
    public void setAdresseLigne1(String adresseLigne1) { this.adresseLigne1 = adresseLigne1; }

    public String getAdresseLigne2() { return adresseLigne2; }
    public void setAdresseLigne2(String adresseLigne2) { this.adresseLigne2 = adresseLigne2; }

    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getUrgenceNom() { return urgenceNom; }
    public void setUrgenceNom(String urgenceNom) { this.urgenceNom = urgenceNom; }

    public String getUrgenceLien() { return urgenceLien; }
    public void setUrgenceLien(String urgenceLien) { this.urgenceLien = urgenceLien; }

    public String getUrgenceTelephone() { return urgenceTelephone; }
    public void setUrgenceTelephone(String urgenceTelephone) { this.urgenceTelephone = urgenceTelephone; }

    public String getBanqueNom() { return banqueNom; }
    public void setBanqueNom(String banqueNom) { this.banqueNom = banqueNom; }

    public String getRibCodeBanque() { return ribCodeBanque; }
    public void setRibCodeBanque(String ribCodeBanque) { this.ribCodeBanque = ribCodeBanque; }

    public String getRibCodeGuichet() { return ribCodeGuichet; }
    public void setRibCodeGuichet(String ribCodeGuichet) { this.ribCodeGuichet = ribCodeGuichet; }

    public String getRibNumeroCompte() { return ribNumeroCompte; }
    public void setRibNumeroCompte(String ribNumeroCompte) { this.ribNumeroCompte = ribNumeroCompte; }

    public String getRibCle() { return ribCle; }
    public void setRibCle(String ribCle) { this.ribCle = ribCle; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getTitulaireCompte() { return titulaireCompte; }
    public void setTitulaireCompte(String titulaireCompte) { this.titulaireCompte = titulaireCompte; }

    public SituationFamiliale getSituationFamiliale() { return situationFamiliale; }
    public void setSituationFamiliale(SituationFamiliale situationFamiliale) { this.situationFamiliale = situationFamiliale; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
