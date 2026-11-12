package com.itu.gestionRH.service.documents;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.documents.Document;
import com.itu.gestionRH.entity.enums.DocumentConfidentialite;
import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.DocumentStatut;
import com.itu.gestionRH.entity.enums.DocumentValidation;
import com.itu.gestionRH.entity.enums.AuditAction;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.service.audit.AuditService;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import com.itu.gestionRH.entity.enums.LogAccesType;
import com.itu.gestionRH.entity.enums.TypeDocument;
import com.itu.gestionRH.entity.security.LogAccesDocument;
import com.itu.gestionRH.repository.documents.DocumentRepository;
import com.itu.gestionRH.repository.security.LogAccesDocumentRepository;
import com.itu.gestionRH.security.AccessScope;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class DocumentService {
    public record DocumentDto(Long id, String nomFichier, TypeDocument typeDocument, DocumentEntite entiteRattachement,
            Long entiteId, String typeMime, Long tailleOctets, Integer numeroVersion, Long documentPrecedentId,
            DocumentConfidentialite confidentialite, DocumentStatut statutDocument, LocalDateTime dateCreation,
            LocalDate dateEmission, LocalDate dateExpiration, DocumentValidation statutValidation, Long valideParId,
            LocalDateTime dateValidation, String motifRefus) {}
    public record FichierTelecharge(DocumentDto document, InputStreamResource contenu) {}

    private static final Set<String> EXTENSIONS = Set.of("pdf", "docx", "png", "jpg");
    private final DocumentRepository documents;
    private final LogAccesDocumentRepository logs;
    private final StockageGed stockage;
    private final AntivirusScanner antivirus;
    private final AccessScope accessScope;
    private final AuditService audit;
    private final UtilisateurRepository utilisateurs;
    private final long tailleMax;

    public DocumentService(DocumentRepository documents, LogAccesDocumentRepository logs, StockageGed stockage,
            AntivirusScanner antivirus, AccessScope accessScope, AuditService audit, UtilisateurRepository utilisateurs,
            @Value("${app.ged.max-size-bytes:5242880}") long tailleMax) {
        this.documents=documents; this.logs=logs; this.stockage=stockage; this.antivirus=antivirus; this.accessScope=accessScope;
        this.audit=audit; this.utilisateurs=utilisateurs; this.tailleMax=tailleMax;
    }

    public DocumentDto televerser(MultipartFile fichier, TypeDocument type, DocumentEntite entite, Long entiteId, HttpServletRequest request) {
        return televerser(fichier, type, entite, entiteId, null, null, request);
    }

    public DocumentDto televerser(MultipartFile fichier, TypeDocument type, DocumentEntite entite, Long entiteId,
            LocalDate dateEmission, LocalDate dateExpiration, HttpServletRequest request) {
        accessScope.requireDocumentTarget(entite, entiteId);
        verifierDates(dateEmission, dateExpiration);
        Valide val = valider(fichier, type);
        Document d = Document.nouvelle();
        remplir(d, fichier, val, type, entite, entiteId);
        d.setNumeroVersion(1);
        d.setDocumentPrecedent(null);
        d.setConfidentialite(confidentialiteParDefaut(type));
        d.setStatutDocument(DocumentStatut.ACTIF);
        d.setDateEmission(dateEmission);
        d.setDateExpiration(dateExpiration);
        d.setStatutValidation(DocumentValidation.A_VALIDER);
        d.setUrlStockageGed(stocker(entite, entiteId, val.bytes()));
        Document saved = documents.saveAndFlush(d);
        journaliser(saved, LogAccesType.TELEVERSEMENT, request);
        tracerDossier(AuditAction.CREATION, saved, "Dépôt (" + type.name() + ", v1)");
        return dto(saved);
    }

    public List<DocumentDto> lister(DocumentEntite entite, Long entiteId, HttpServletRequest request) {
        if (entite == null || entiteId == null) throw new BusinessRuleException("DOCUMENT_CIBLE_REQUISE", "L’entité et son identifiant sont obligatoires.");
        List<Document> items = documents.findByEntiteRattachementAndEntiteIdAndStatutDocumentNotOrderByNumeroVersionDesc(entite, entiteId, DocumentStatut.SUPPRIME);
        return items.stream().filter(this::accessibleSansErreur).peek(d -> journaliser(d, LogAccesType.CONSULTATION, request)).map(this::dto).toList();
    }

    public List<DocumentDto> aValider(HttpServletRequest request) {
        return documents.findByStatutValidationAndStatutDocumentOrderByDateCreationAsc(
                        DocumentValidation.A_VALIDER, DocumentStatut.ACTIF).stream()
                .filter(this::accessibleSansErreur)
                .peek(d -> journaliser(d, LogAccesType.CONSULTATION, request))
                .map(this::dto).toList();
    }

    public FichierTelecharge telecharger(Long id, HttpServletRequest request) {
        Document d = accessible(id);
        InputStream stream;
        try { stream = stockage.ouvrir(d.getUrlStockageGed()); }
        catch (IOException | SecurityException ex) { throw new IllegalStateException("Le fichier demandé est indisponible."); }
        journaliser(d, LogAccesType.TELECHARGEMENT, request);
        return new FichierTelecharge(dto(d), new InputStreamResource(stream));
    }

    public DocumentDto remplacer(Long id, MultipartFile fichier, HttpServletRequest request) {
        return remplacer(id, fichier, null, null, request);
    }

    /** Nouvelle version : elle repart « à valider » et porte ses propres dates de validité. */
    public DocumentDto remplacer(Long id, MultipartFile fichier, LocalDate dateEmission, LocalDate dateExpiration, HttpServletRequest request) {
        Document precedent = accessible(id);
        verifierDates(dateEmission, dateExpiration);
        if (precedent.getStatutDocument() != DocumentStatut.ACTIF) throw new BusinessRuleException("DOCUMENT_NON_ACTIF", "Seule la version active peut être remplacée.");
        Valide val = valider(fichier, precedent.getTypeDocument());
        Document nouvelleVersion = Document.nouvelle();
        remplir(nouvelleVersion, fichier, val, precedent.getTypeDocument(), precedent.getEntiteRattachement(), precedent.getEntiteId());
        nouvelleVersion.setNumeroVersion(precedent.getNumeroVersion() + 1);
        nouvelleVersion.setDocumentPrecedent(precedent);
        nouvelleVersion.setConfidentialite(precedent.getConfidentialite());
        nouvelleVersion.setStatutDocument(DocumentStatut.ACTIF);
        nouvelleVersion.setDateEmission(dateEmission);
        nouvelleVersion.setDateExpiration(dateExpiration);
        nouvelleVersion.setStatutValidation(DocumentValidation.A_VALIDER);
        nouvelleVersion.setUrlStockageGed(stocker(precedent.getEntiteRattachement(), precedent.getEntiteId(), val.bytes()));
        precedent.setStatutDocument(DocumentStatut.ARCHIVE);
        documents.save(precedent);
        Document saved = documents.saveAndFlush(nouvelleVersion);
        journaliser(saved, LogAccesType.TELEVERSEMENT, request);
        tracerDossier(AuditAction.CREATION, saved, "Nouvelle version v" + saved.getNumeroVersion() + " (remplace #" + precedent.getId() + ")");
        return dto(saved);
    }

    public void archiver(Long id, HttpServletRequest request) {
        Document d = accessible(id);
        if (d.getStatutDocument() == DocumentStatut.ACTIF) {
            d.setStatutDocument(DocumentStatut.ARCHIVE);
            documents.save(d);
        }
        journaliser(d, LogAccesType.SUPPRESSION, request);
        tracerDossier(AuditAction.SUPPRESSION, d, "Archivage du document");
    }

    // ---- validation, confidentialité, validité (Bloc 5)

    public DocumentDto valider(Long id) {
        Document d = documentATraiter(id);
        if (d.getDateExpiration() != null && d.getDateExpiration().isBefore(LocalDate.now()))
            throw new BusinessRuleException("DOCUMENT_EXPIRE", "Un document expiré ne peut pas être validé : demandez une nouvelle version.");
        decider(d, DocumentValidation.VALIDE, null);
        tracerChamp(AuditAction.VALIDATION, d, "statutValidation", DocumentValidation.A_VALIDER, DocumentValidation.VALIDE, null);
        return dto(documents.saveAndFlush(d));
    }

    public DocumentDto refuser(Long id, String motif) {
        if (motif == null || motif.isBlank()) throw new BusinessRuleException("MOTIF_REFUS_REQUIS", "Le motif du refus est obligatoire.", HttpStatus.BAD_REQUEST);
        Document d = documentATraiter(id);
        String m = motif.trim().length() <= 500 ? motif.trim() : motif.trim().substring(0, 500);
        decider(d, DocumentValidation.REFUSE, m);
        tracerChamp(AuditAction.REFUS, d, "statutValidation", DocumentValidation.A_VALIDER, DocumentValidation.REFUSE, m);
        return dto(documents.saveAndFlush(d));
    }

    /** Qui voit quoi : STRICT exige DOCUMENT_STRICT_CONSULTER, aussi bien pour y placer un document que pour l'en sortir. */
    public DocumentDto changerConfidentialite(Long id, DocumentConfidentialite niveau) {
        if (niveau == null) throw new BusinessRuleException("CONFIDENTIALITE_REQUISE", "Le niveau de confidentialité est obligatoire.", HttpStatus.BAD_REQUEST);
        Document d = accessible(id);
        if (d.getStatutDocument() == DocumentStatut.SUPPRIME) throw new NotFoundException("Document introuvable.");
        boolean strict = d.getConfidentialite() == DocumentConfidentialite.STRICT || niveau == DocumentConfidentialite.STRICT;
        if (strict && !accessScope.hasPermission("DOCUMENT_STRICT_CONSULTER") && !accessScope.hasPermission("UTILISATEUR_ADMIN"))
            throw new BusinessRuleException("NIVEAU_STRICT_RESERVE", "Le niveau « strict » est réservé aux profils habilités.", HttpStatus.FORBIDDEN);
        DocumentConfidentialite avant = d.getConfidentialite();
        if (avant == niveau) return dto(d);
        d.setConfidentialite(niveau);
        tracerChamp(AuditAction.MODIFICATION, d, "confidentialite", avant, niveau, null);
        return dto(documents.saveAndFlush(d));
    }

    /** Corrige les dates de validité d'une pièce sans nouvelle version ; la pièce repasse « à valider ». */
    public DocumentDto modifierValidite(Long id, LocalDate dateEmission, LocalDate dateExpiration) {
        Document d = accessible(id);
        if (d.getStatutDocument() != DocumentStatut.ACTIF) throw new BusinessRuleException("DOCUMENT_NON_ACTIF", "Seule la version active peut être modifiée.");
        verifierDates(dateEmission, dateExpiration);
        var diff = new AuditService.Diff().champ("dateEmission", d.getDateEmission(), dateEmission)
                .champ("dateExpiration", d.getDateExpiration(), dateExpiration);
        if (diff.vide()) return dto(d);
        d.setDateEmission(dateEmission);
        d.setDateExpiration(dateExpiration);
        if (d.getStatutValidation() != DocumentValidation.A_VALIDER) {
            diff.champ("statutValidation", d.getStatutValidation(), DocumentValidation.A_VALIDER);
            d.setStatutValidation(DocumentValidation.A_VALIDER);
            d.setValidePar(null); d.setDateValidation(null); d.setMotifRefus(null);
        }
        Document saved = documents.saveAndFlush(d);
        if (estDossier(saved)) audit.tracer(AuditAction.MODIFICATION, "DOCUMENT", saved.getId(), employeDe(saved), diff.liste(), null);
        return dto(saved);
    }

    private Document documentATraiter(Long id) {
        Document d = accessible(id);
        if (d.getStatutDocument() != DocumentStatut.ACTIF) throw new BusinessRuleException("DOCUMENT_NON_ACTIF", "Seule la version active peut être validée ou refusée.");
        if (d.getEntiteRattachement() != DocumentEntite.EMPLOYE && d.getEntiteRattachement() != DocumentEntite.CONTRAT
                && d.getEntiteRattachement() != DocumentEntite.CANDIDAT && d.getEntiteRattachement() != DocumentEntite.CANDIDATURE)
            throw new BusinessRuleException("VALIDATION_NON_APPLICABLE", "La validation ne concerne pas ce type de document.");
        if (d.getEntiteRattachement() == DocumentEntite.EMPLOYE && accessScope.estSoiMeme(d.getEntiteId()))
            throw new BusinessRuleException("AUTO_VALIDATION_INTERDITE", "Vous ne pouvez pas valider vos propres pièces.", HttpStatus.FORBIDDEN);
        if (d.getStatutValidation() != DocumentValidation.A_VALIDER)
            throw new BusinessRuleException("DOCUMENT_DEJA_TRAITE", "Cette pièce a déjà été traitée. Déposez une nouvelle version pour une nouvelle décision.");
        return d;
    }

    private void decider(Document d, DocumentValidation decision, String motif) {
        d.setStatutValidation(decision);
        d.setValidePar(utilisateurs.findById(accessScope.currentUserId()).orElse(null));
        d.setDateValidation(LocalDateTime.now());
        d.setMotifRefus(motif);
    }

    private void verifierDates(LocalDate emission, LocalDate expiration) {
        if (emission != null && emission.isAfter(LocalDate.now()))
            throw new BusinessRuleException("DATE_EMISSION_INVALIDE", "La date d'émission ne peut pas être dans le futur.", HttpStatus.BAD_REQUEST);
        if (emission != null && expiration != null && expiration.isBefore(emission))
            throw new BusinessRuleException("DATES_VALIDITE_INVALIDES", "La date d'expiration précède la date d'émission.", HttpStatus.BAD_REQUEST);
    }

    private boolean estDossier(Document d) {
        return d.getEntiteRattachement() == DocumentEntite.EMPLOYE || d.getEntiteRattachement() == DocumentEntite.CONTRAT;
    }
    private Long employeDe(Document d) { return d.getEntiteRattachement() == DocumentEntite.EMPLOYE ? d.getEntiteId() : null; }
    private void tracerDossier(AuditAction action, Document d, String motif) {
        if (estDossier(d)) audit.tracer(action, "DOCUMENT", d.getId(), employeDe(d), motif);
    }
    private void tracerChamp(AuditAction action, Document d, String champ, Object avant, Object apres, String motif) {
        audit.tracer(action, "DOCUMENT", d.getId(), employeDe(d),
                List.of(new AuditService.Changement(champ, String.valueOf(avant), String.valueOf(apres), false)), motif);
    }

    private Document accessible(Long id) {
        Document d = documents.findById(id).orElseThrow(() -> new NotFoundException("Document introuvable."));
        return accessScope.requireDocument(id);
    }
    private boolean accessibleSansErreur(Document d) {
        try { accessScope.requireDocument(d.getId()); return true; }
        catch (NotFoundException ex) { return false; }
    }

    private record Valide(byte[] bytes, String filename, String extension, String mime) {}
    private Valide valider(MultipartFile fichier, TypeDocument type) {
        if (type == null) throw new BusinessRuleException("TYPE_DOCUMENT_INVALIDE", "Le type de document est obligatoire.");
        if (fichier == null || fichier.isEmpty()) throw new BusinessRuleException("FICHIER_VIDE", "Sélectionnez un fichier non vide.");
        if (tailleMax <= 0 || fichier.getSize() > tailleMax) throw new BusinessRuleException("FICHIER_TROP_VOLUMINEUX", "Le fichier dépasse la taille maximale autorisée.");
        String filename = assainirNom(fichier.getOriginalFilename());
        String extension = extension(filename);
        if (!EXTENSIONS.contains(extension) || !extensionsAutorisees(type).contains(extension))
            throw new BusinessRuleException("TYPE_FICHIER_REFUSE", "Le format de fichier n’est pas autorisé pour ce document.");
        byte[] bytes;
        try { bytes = fichier.getBytes(); }
        catch (IOException ex) { throw new BusinessRuleException("LECTURE_FICHIER_ECHEC", "Le fichier n’a pas pu être lu."); }
        if (bytes.length == 0 || bytes.length > tailleMax) throw new BusinessRuleException("FICHIER_TROP_VOLUMINEUX", "Le fichier dépasse la taille maximale autorisée.");
        String mime = detecterMime(bytes);
        if (mime == null || !extension.equals(extensionPourMime(mime)))
            throw new BusinessRuleException("CONTENU_FICHIER_INVALIDE", "Le contenu du fichier ne correspond pas à son extension.");
        antivirus.verifier(bytes);
        return new Valide(bytes, filename, extension, mime);
    }

    private Set<String> extensionsAutorisees(TypeDocument type) {
        return switch (type) {
            case CV, LETTRE_MOTIVATION, CONTRAT_SIGNE, FICHE_POSTE, PROPOSITION, AVENANT_SIGNE -> Set.of("pdf", "docx");
            case RIB, PIECE_IDENTITE, CARTE_VITALE, REGLEMENT_SIGNE, AUTRE, DIPLOME, PERMIS_TRAVAIL, CERTIFICAT_MEDICAL,
                    ACTE_NAISSANCE, JUSTIFICATIF_DOMICILE, CASIER_JUDICIAIRE, ATTESTATION_CNAPS -> EXTENSIONS;
        };
    }
    private String detecterMime(byte[] b) {
        if (b.length >= 5 && b[0]=='%' && b[1]=='P' && b[2]=='D' && b[3]=='F' && b[4]=='-') return "application/pdf";
        if (b.length >= 8 && (b[0]&255)==0x89 && b[1]=='P' && b[2]=='N' && b[3]=='G' && b[4]==13 && b[5]==10 && b[6]==26 && b[7]==10) return "image/png";
        if (b.length >= 3 && (b[0]&255)==0xff && (b[1]&255)==0xd8 && (b[2]&255)==0xff) return "image/jpeg";
        if (b.length >= 4 && b[0]=='P' && b[1]=='K' && b[2]==3 && b[3]==4 && estDocx(b)) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return null;
    }
    private boolean estDocx(byte[] bytes) {
        boolean contentTypes = false, wordDocument = false;
        try (ZipInputStream zip = new ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("[Content_Types].xml".equals(entry.getName())) contentTypes = true;
                if ("word/document.xml".equals(entry.getName())) wordDocument = true;
                if (contentTypes && wordDocument) return true;
            }
        } catch (IOException ignored) { return false; }
        return false;
    }
    private String extensionPourMime(String mime) {
        return switch (mime) {
            case "application/pdf" -> "pdf";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            default -> "";
        };
    }
    private String assainirNom(String raw) {
        String name = StringUtils.cleanPath(raw == null ? "document" : raw).replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").replaceAll("[^\\p{L}\\p{N}._ -]", "_").trim();
        if (name.isBlank() || name.equals(".") || name.equals("..")) throw new BusinessRuleException("NOM_FICHIER_INVALIDE", "Le nom du fichier est invalide.");
        if (name.length() > 255) name = name.substring(0, 255);
        return name;
    }
    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
    private DocumentConfidentialite confidentialiteParDefaut(TypeDocument type) {
        return switch (type) {
            case LETTRE_MOTIVATION, FICHE_POSTE, PROPOSITION, AUTRE -> DocumentConfidentialite.INTERNE;
            default -> DocumentConfidentialite.CONFIDENTIEL;
        };
    }
    private void remplir(Document d, MultipartFile original, Valide val, TypeDocument type, DocumentEntite entite, Long entiteId) {
        d.setNomFichier(val.filename()); d.setTypeDocument(type); d.setEntiteRattachement(entite); d.setEntiteId(entiteId);
        d.setTypeMime(val.mime()); d.setTailleOctets((long) val.bytes().length); d.setEmpreinteSha256(sha256(val.bytes()));
    }
    private String stocker(DocumentEntite entite, Long entiteId, byte[] bytes) {
        try { return stockage.stocker(entite.name(), entiteId, bytes); }
        catch (IOException ex) { throw new IllegalStateException("Le fichier n’a pas pu être stocké."); }
    }
    private String sha256(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("Calcul d’empreinte indisponible."); }
    }
    private void journaliser(Document d, LogAccesType type, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth == null || !auth.isAuthenticated() ? "ANONYME" : auth.getName();
        LogAccesDocument log = LogAccesDocument.nouvelle();
        log.enregistrer(d, login, type, LocalDateTime.now(), request == null ? null : request.getRemoteAddr());
        logs.save(log);
    }
    private DocumentDto dto(Document d) {
        Long precedent = d.getDocumentPrecedent() == null ? null : d.getDocumentPrecedent().getId();
        return new DocumentDto(d.getId(), d.getNomFichier(), d.getTypeDocument(), d.getEntiteRattachement(), d.getEntiteId(),
                d.getTypeMime(), d.getTailleOctets(), d.getNumeroVersion(), precedent, d.getConfidentialite(), d.getStatutDocument(), d.getDateCreation(),
                d.getDateEmission(), d.getDateExpiration(), d.getStatutValidation(),
                d.getValidePar() == null ? null : d.getValidePar().getId(), d.getDateValidation(), d.getMotifRefus());
    }
}
