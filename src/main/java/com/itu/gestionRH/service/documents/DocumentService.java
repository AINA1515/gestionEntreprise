package com.itu.gestionRH.service.documents;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.documents.Document;
import com.itu.gestionRH.entity.enums.DocumentConfidentialite;
import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.DocumentStatut;
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
            DocumentConfidentialite confidentialite, DocumentStatut statutDocument, LocalDateTime dateCreation) {}
    public record FichierTelecharge(DocumentDto document, InputStreamResource contenu) {}

    private static final Set<String> EXTENSIONS = Set.of("pdf", "docx", "png", "jpg");
    private final DocumentRepository documents;
    private final LogAccesDocumentRepository logs;
    private final StockageGed stockage;
    private final AntivirusScanner antivirus;
    private final AccessScope accessScope;
    private final long tailleMax;

    public DocumentService(DocumentRepository documents, LogAccesDocumentRepository logs, StockageGed stockage,
            AntivirusScanner antivirus, AccessScope accessScope, @Value("${app.ged.max-size-bytes:5242880}") long tailleMax) {
        this.documents=documents; this.logs=logs; this.stockage=stockage; this.antivirus=antivirus; this.accessScope=accessScope; this.tailleMax=tailleMax;
    }

    public DocumentDto televerser(MultipartFile fichier, TypeDocument type, DocumentEntite entite, Long entiteId, HttpServletRequest request) {
        accessScope.requireDocumentTarget(entite, entiteId);
        Valide val = valider(fichier, type);
        Document d = Document.nouvelle();
        remplir(d, fichier, val, type, entite, entiteId);
        d.setNumeroVersion(1);
        d.setDocumentPrecedent(null);
        d.setConfidentialite(confidentialiteParDefaut(type));
        d.setStatutDocument(DocumentStatut.ACTIF);
        d.setUrlStockageGed(stocker(entite, entiteId, val.bytes()));
        Document saved = documents.saveAndFlush(d);
        journaliser(saved, LogAccesType.TELEVERSEMENT, request);
        return dto(saved);
    }

    public List<DocumentDto> lister(DocumentEntite entite, Long entiteId, HttpServletRequest request) {
        if (entite == null || entiteId == null) throw new BusinessRuleException("DOCUMENT_CIBLE_REQUISE", "L’entité et son identifiant sont obligatoires.");
        List<Document> items = documents.findByEntiteRattachementAndEntiteIdAndStatutDocumentNotOrderByNumeroVersionDesc(entite, entiteId, DocumentStatut.SUPPRIME);
        return items.stream().filter(this::accessibleSansErreur).peek(d -> journaliser(d, LogAccesType.CONSULTATION, request)).map(this::dto).toList();
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
        Document precedent = accessible(id);
        if (precedent.getStatutDocument() != DocumentStatut.ACTIF) throw new BusinessRuleException("DOCUMENT_NON_ACTIF", "Seule la version active peut être remplacée.");
        Valide val = valider(fichier, precedent.getTypeDocument());
        Document nouvelleVersion = Document.nouvelle();
        remplir(nouvelleVersion, fichier, val, precedent.getTypeDocument(), precedent.getEntiteRattachement(), precedent.getEntiteId());
        nouvelleVersion.setNumeroVersion(precedent.getNumeroVersion() + 1);
        nouvelleVersion.setDocumentPrecedent(precedent);
        nouvelleVersion.setConfidentialite(precedent.getConfidentialite());
        nouvelleVersion.setStatutDocument(DocumentStatut.ACTIF);
        nouvelleVersion.setUrlStockageGed(stocker(precedent.getEntiteRattachement(), precedent.getEntiteId(), val.bytes()));
        precedent.setStatutDocument(DocumentStatut.ARCHIVE);
        documents.save(precedent);
        Document saved = documents.saveAndFlush(nouvelleVersion);
        journaliser(saved, LogAccesType.TELEVERSEMENT, request);
        return dto(saved);
    }

    public void archiver(Long id, HttpServletRequest request) {
        Document d = accessible(id);
        if (d.getStatutDocument() == DocumentStatut.ACTIF) {
            d.setStatutDocument(DocumentStatut.ARCHIVE);
            documents.save(d);
        }
        journaliser(d, LogAccesType.SUPPRESSION, request);
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
            case CV, LETTRE_MOTIVATION, CONTRAT_SIGNE, FICHE_POSTE, PROPOSITION -> Set.of("pdf", "docx");
            case RIB, PIECE_IDENTITE, CARTE_VITALE, REGLEMENT_SIGNE, AUTRE -> EXTENSIONS;
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
            case CV, CONTRAT_SIGNE, RIB, PIECE_IDENTITE, CARTE_VITALE, REGLEMENT_SIGNE -> DocumentConfidentialite.CONFIDENTIEL;
            default -> DocumentConfidentialite.INTERNE;
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
                d.getTypeMime(), d.getTailleOctets(), d.getNumeroVersion(), precedent, d.getConfidentialite(), d.getStatutDocument(), d.getDateCreation());
    }
}
