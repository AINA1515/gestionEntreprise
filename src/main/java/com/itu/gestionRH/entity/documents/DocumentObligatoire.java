package com.itu.gestionRH.entity.documents;

import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.enums.TypeDocument;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/** Pièce exigée dans un dossier salarié (table DOCUMENT_OBLIGATOIRE, migration 015). typeContrat null = tous les contrats. */
@Entity
@Table(name = "DOCUMENT_OBLIGATOIRE")
public class DocumentObligatoire {
    protected DocumentObligatoire() {}
    public static DocumentObligatoire nouvelle() { return new DocumentObligatoire(); }

    @Column(name = "ID", precision = 10, scale = 0)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "documentObligatoireGen")
    @SequenceGenerator(name = "documentObligatoireGen", sequenceName = "SEQ_DOCUMENT_OBLIGATOIRE", allocationSize = 1)
    private Long id;

    @Column(name = "TYPE_DOCUMENT", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;

    @Column(name = "TYPE_CONTRAT", length = 12)
    @Enumerated(EnumType.STRING)
    private ContratType typeContrat;

    @Column(name = "LIBELLE", nullable = false, length = 150)
    private String libelle;

    @Column(name = "DELAI_ALERTE_JOURS", nullable = false)
    private Integer delaiAlerteJours;

    @Column(name = "ORDRE", nullable = false)
    private Integer ordre;

    @Column(name = "EST_ACTIF", nullable = false)
    private Boolean estActif;

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
    public TypeDocument getTypeDocument() { return typeDocument; }
    public void setTypeDocument(TypeDocument typeDocument) { this.typeDocument = typeDocument; }
    public ContratType getTypeContrat() { return typeContrat; }
    public void setTypeContrat(ContratType typeContrat) { this.typeContrat = typeContrat; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Integer getDelaiAlerteJours() { return delaiAlerteJours; }
    public void setDelaiAlerteJours(Integer delaiAlerteJours) { this.delaiAlerteJours = delaiAlerteJours; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public Boolean getEstActif() { return estActif; }
    public void setEstActif(Boolean estActif) { this.estActif = estActif; }
    public Long getVersionLigne() { return versionLigne; }
}
