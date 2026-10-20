package com.itu.gestionRH.entity.documents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "DOCUMENT")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "documentGen")
    @SequenceGenerator(name = "documentGen", sequenceName = "SEQ_DOCUMENT", allocationSize = 1)
    private Long id;

    @Column(name = "NOM_FICHIER", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "TYPE_DOCUMENT", nullable = false, length = 25)
    private String typeDocument;

    @Column(name = "ENTITE_RATTACHEMENT", nullable = false, length = 30)
    private String entiteRattachement;

    @Column(name = "ENTITE_ID", nullable = false)
    private Long entiteId;

    @Column(name = "URL_STOCKAGE_GED", nullable = false, length = 1000)
    private String urlStockageGed;

    @Column(name = "TYPE_MIME", length = 100)
    private String typeMime;

    @Column(name = "TAILLE_OCTETS")
    private Long tailleOctets;

    @Column(name = "EMPREINTE_SHA256", length = 64)
    private String empreinteSha256;

    @Column(name = "NUMERO_VERSION")
    private Integer numeroVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOCUMENT_PRECEDENT_ID")
    private Document documentPrecedent;

    @Column(name = "CONFIDENTIALITE", nullable = false, length = 12)
    private String confidentialite;

    @Column(name = "STATUT_DOCUMENT", nullable = false, length = 10)
    private String statutDocument;

    // Colonnes d'audit gérées par le trigger TBR_DOCUMENT (script GC-01)
    @Column(name = "DATE_CREATION", insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "CREE_PAR", insertable = false, updatable = false)
    private String creePar;

    @Column(name = "DATE_MODIFICATION", insertable = false, updatable = false)
    private LocalDateTime dateModification;

    @Column(name = "MODIFIE_PAR", insertable = false, updatable = false)
    private String modifiePar;

    @Column(name = "VERSION_LIGNE", insertable = false, updatable = false)
    private Long versionLigne;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }

    public String getEntiteRattachement() { return entiteRattachement; }
    public void setEntiteRattachement(String entiteRattachement) { this.entiteRattachement = entiteRattachement; }

    public Long getEntiteId() { return entiteId; }
    public void setEntiteId(Long entiteId) { this.entiteId = entiteId; }

    public String getUrlStockageGed() { return urlStockageGed; }
    public void setUrlStockageGed(String urlStockageGed) { this.urlStockageGed = urlStockageGed; }

    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }

    public Long getTailleOctets() { return tailleOctets; }
    public void setTailleOctets(Long tailleOctets) { this.tailleOctets = tailleOctets; }

    public String getEmpreinteSha256() { return empreinteSha256; }
    public void setEmpreinteSha256(String empreinteSha256) { this.empreinteSha256 = empreinteSha256; }

    public Integer getNumeroVersion() { return numeroVersion; }
    public void setNumeroVersion(Integer numeroVersion) { this.numeroVersion = numeroVersion; }

    public Document getDocumentPrecedent() { return documentPrecedent; }
    public void setDocumentPrecedent(Document documentPrecedent) { this.documentPrecedent = documentPrecedent; }

    public String getConfidentialite() { return confidentialite; }
    public void setConfidentialite(String confidentialite) { this.confidentialite = confidentialite; }

    public String getStatutDocument() { return statutDocument; }
    public void setStatutDocument(String statutDocument) { this.statutDocument = statutDocument; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getCreePar() { return creePar; }
    public void setCreePar(String creePar) { this.creePar = creePar; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public String getModifiePar() { return modifiePar; }
    public void setModifiePar(String modifiePar) { this.modifiePar = modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
    public void setVersionLigne(Long versionLigne) { this.versionLigne = versionLigne; }
}
