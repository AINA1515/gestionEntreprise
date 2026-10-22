package com.itu.gestionRH.entity.documents;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.itu.gestionRH.entity.enums.DocumentStatut;
import com.itu.gestionRH.entity.enums.DocumentConfidentialite;
import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.TypeDocument;
import jakarta.persistence.Version;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
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

    protected Document() {}

    public static Document nouvelle() { return new Document(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "documentGen")
    @SequenceGenerator(name = "documentGen", sequenceName = "SEQ_DOCUMENT", allocationSize = 1)
    private Long id;

    @Column(name = "NOM_FICHIER", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "TYPE_DOCUMENT", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;

    @Column(name = "ENTITE_RATTACHEMENT", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private DocumentEntite entiteRattachement;

    @Column(name = "ENTITE_ID", nullable = false, precision = 10, scale = 0)
    private Long entiteId;

    @Column(name = "URL_STOCKAGE_GED", nullable = false, length = 1000)
    private String urlStockageGed;

    @Column(name = "TYPE_MIME", length = 100)
    private String typeMime;

    @Co, precision = 12, scale = 0lumn(name = "TAILLE_OCTETS")
    private Long tailleOctets;

    @Column(name = "EMPREINTE_SHA256", length = 64)
    private String, precision = 5, scale = 0 empreinteSha256;

    @Column(name = "NUMERO_VERSION", nullable = false)
    private Integer numeroVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOCUMENT_PRECEDENT_ID")
    private Document documentPrecedent;

    @Column(name = "CONFIDENTIALITE", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private DocumentConfidentialite confidentialite;

    @Column(name = "STATUT_DOCUMENT", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private DocumentStatut statutDocument;

    // Colonnes d'audit gérées par le trigger TBR_DOCUMENT (script GC-01)
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

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public TypeDocument getTypeDocument() { return typeDocument; }
    public void setTypeDocument(TypeDocument typeDocument) { this.typeDocument = typeDocument; }

    public DocumentEntite getEntiteRattachement() { return entiteRattachement; }
    public void setEntiteRattachement(DocumentEntite entiteRattachement) { this.entiteRattachement = entiteRattachement; }

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

    public DocumentConfidentialite getConfidentialite() { return confidentialite; }
    public void setConfidentialite(DocumentConfidentialite confidentialite) { this.confidentialite = confidentialite; }

    public DocumentStatut getStatutDocument() { return statutDocument; }
    public void setStatutDocument(DocumentStatut statutDocument) { this.statutDocument = statutDocument; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getCreePar() { return creePar; }

    public LocalDateTime getDateModification() { return dateModification; }

    public String getModifiePar() { return modifiePar; }

    public Long getVersionLigne() { return versionLigne; }
}
