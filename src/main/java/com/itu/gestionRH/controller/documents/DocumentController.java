package com.itu.gestionRH.controller.documents;

import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.TypeDocument;
import com.itu.gestionRH.service.documents.DocumentService;
import com.itu.gestionRH.repository.security.CandidatRepository;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import com.itu.gestionRH.entity.enums.DocumentConfidentialite;
import com.itu.gestionRH.entity.documents.DocumentObligatoire;
import com.itu.gestionRH.repository.documents.DocumentObligatoireRepository;
import com.itu.gestionRH.common.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private static final String DOCUMENT_ACCESS = "hasAnyAuthority('PERM_DOCUMENT_CONSULTER_TOUS','PERM_DOCUMENT_CONSULTER_PERSO','PERM_CANDIDATURE_PRESELECTIONNER','PERM_EVALUATION_SAISIR','PERM_DEMANDE_CREER','PERM_CONTRAT_GERER','PERM_ONBOARDING_ADMIN','PERM_TACHE_TRAITER','PERM_OFFRE_PUBLIER','PERM_PROPOSITION_EMETTRE','PERM_UTILISATEUR_ADMIN')";
    private final DocumentService service;
    private final DocumentObligatoireRepository obligatoires;
    private final CandidatRepository candidats;
    private final CandidatureRepository candidatures;

    public record ObligatoireDto(Long id, String typeDocument, String typeContrat, String libelle, Integer delaiAlerteJours, Integer ordre, Boolean estActif) {}
    public record DocumentAValiderDto(DocumentService.DocumentDto document, String candidat, String email,
            String offre, Long candidatureId, Long candidatId) {}
    public record RefusRequest(@NotBlank @Size(max = 500) String motif) {}
    public record ConfidentialiteRequest(DocumentConfidentialite niveau) {}
    public record ValiditeRequest(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEmission,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateExpiration) {}
    public record ObligatoirePatch(@Min(0) @Max(365) Integer delaiAlerteJours, Boolean estActif) {}

    public DocumentController(DocumentService service, DocumentObligatoireRepository obligatoires,
            CandidatRepository candidats, CandidatureRepository candidatures) {
        this.service = service; this.obligatoires = obligatoires; this.candidats = candidats; this.candidatures = candidatures;
    }

    @PreAuthorize(DOCUMENT_ACCESS)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentService.DocumentDto> televerser(@RequestPart("fichier") MultipartFile fichier,
            @RequestParam TypeDocument typeDocument, @RequestParam DocumentEntite entiteRattachement,
            @RequestParam Long entiteId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEmission,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateExpiration, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.televerser(fichier, typeDocument, entiteRattachement, entiteId, dateEmission, dateExpiration, request));
    }

    @PreAuthorize(DOCUMENT_ACCESS)
    @GetMapping
    public List<DocumentService.DocumentDto> lister(@RequestParam DocumentEntite entite, @RequestParam Long id, HttpServletRequest request) {
        return service.lister(entite, id, request);
    }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_VALIDER')")
    @GetMapping("/a-valider")
    @Transactional(readOnly = true)
    public List<DocumentAValiderDto> aValider(HttpServletRequest request) {
        return service.aValider(request).stream().map(document -> {
            Long candidatureId = document.entiteRattachement() == DocumentEntite.CANDIDATURE ? document.entiteId() : null;
            var candidature = candidatureId == null ? null : candidatures.findById(candidatureId).orElse(null);
            Long candidatId = document.entiteRattachement() == DocumentEntite.CANDIDAT
                    ? document.entiteId() : candidature == null || candidature.getCandidat() == null ? null : candidature.getCandidat().getId();
            var candidat = candidatId == null ? null : candidats.findById(candidatId).orElse(null);
            return new DocumentAValiderDto(document,
                    candidat == null ? null : ((candidat.getPrenom() == null ? "" : candidat.getPrenom() + " ")
                            + (candidat.getNom() == null ? "" : candidat.getNom())).trim(),
                    candidat == null ? null : candidat.getEmail(),
                    candidature == null || candidature.getOffre() == null ? null : candidature.getOffre().getTitre(),
                    candidatureId, candidatId);
        }).toList();
    }

    @PreAuthorize(DOCUMENT_ACCESS)
    @GetMapping("/{id}/contenu")
    public ResponseEntity<InputStreamResource> telecharger(@PathVariable Long id, HttpServletRequest request) {
        var fichier = service.telecharger(id, request);
        var dto = fichier.document();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.typeMime()));
        headers.setContentLength(dto.tailleOctets());
        headers.setContentDisposition(ContentDisposition.attachment().filename(dto.nomFichier(), StandardCharsets.UTF_8).build());
        return new ResponseEntity<>(fichier.contenu(), headers, HttpStatus.OK);
    }

    @PreAuthorize(DOCUMENT_ACCESS)
    @GetMapping("/{id}/contenu/visualiser")
    public ResponseEntity<InputStreamResource> visualiser(@PathVariable Long id, HttpServletRequest request) {
        var fichier = service.telecharger(id, request);
        var dto = fichier.document();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.typeMime()));
        headers.setContentLength(dto.tailleOctets());
        headers.setContentDisposition(ContentDisposition.inline().filename(dto.nomFichier(), StandardCharsets.UTF_8).build());
        return new ResponseEntity<>(fichier.contenu(), headers, HttpStatus.OK);
    }

    @PreAuthorize(DOCUMENT_ACCESS)
    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentService.DocumentDto> remplacer(@PathVariable Long id,
            @RequestPart("fichier") MultipartFile fichier, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEmission,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateExpiration, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.remplacer(id, fichier, dateEmission, dateExpiration, request));
    }

    @PreAuthorize(DOCUMENT_ACCESS)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiver(@PathVariable Long id, HttpServletRequest request) {
        service.archiver(id, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_VALIDER')")
    @PostMapping("/{id}/validation")
    public DocumentService.DocumentDto valider(@PathVariable Long id) { return service.valider(id); }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_VALIDER')")
    @PostMapping("/{id}/refus")
    public DocumentService.DocumentDto refuser(@PathVariable Long id, @Valid @RequestBody RefusRequest request) {
        return service.refuser(id, request.motif());
    }

    @PreAuthorize("hasAuthority('PERM_DOCUMENT_CONFIDENTIALITE_GERER')")
    @PatchMapping("/{id}/confidentialite")
    public DocumentService.DocumentDto confidentialite(@PathVariable Long id, @RequestBody ConfidentialiteRequest request) {
        return service.changerConfidentialite(id, request.niveau());
    }

    @PreAuthorize("hasAuthority('PERM_DOSSIER_MODIFIER')")
    @PatchMapping("/{id}/validite")
    public DocumentService.DocumentDto validite(@PathVariable Long id, @RequestBody ValiditeRequest request) {
        return service.modifierValidite(id, request.dateEmission(), request.dateExpiration());
    }

    /** Liste des pièces exigées (référentiel), pour l'écran de dépôt et l'état du dossier. */
    @PreAuthorize("hasAnyAuthority('PERM_DOSSIER_CONSULTER_TOUS','PERM_DOSSIER_CONSULTER_PERSO')")
    @GetMapping("/obligatoires")
    @Transactional(readOnly = true)
    public List<ObligatoireDto> obligatoires() { return obligatoires.findAllByOrderByOrdreAscIdAsc().stream().map(this::dto).toList(); }

    @PreAuthorize("hasAuthority('PERM_DOSSIER_MODIFIER')")
    @PatchMapping("/obligatoires/{id}")
    @Transactional
    public ObligatoireDto modifierObligatoire(@PathVariable Long id, @Valid @RequestBody ObligatoirePatch patch) {
        DocumentObligatoire o = obligatoires.findById(id).orElseThrow(() -> new NotFoundException("Pièce obligatoire introuvable."));
        if (patch.delaiAlerteJours() != null) o.setDelaiAlerteJours(patch.delaiAlerteJours());
        if (patch.estActif() != null) o.setEstActif(patch.estActif());
        return dto(obligatoires.saveAndFlush(o));
    }

    private ObligatoireDto dto(DocumentObligatoire o) {
        return new ObligatoireDto(o.getId(), o.getTypeDocument().name(), o.getTypeContrat() == null ? null : o.getTypeContrat().name(),
                o.getLibelle(), o.getDelaiAlerteJours(), o.getOrdre(), o.getEstActif());
    }
}
