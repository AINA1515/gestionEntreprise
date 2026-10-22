package com.itu.gestionRH.controller.documents;

import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.TypeDocument;
import com.itu.gestionRH.service.documents.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
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

    public DocumentController(DocumentService service) { this.service = service; }

    @PreAuthorize(DOCUMENT_ACCESS)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentService.DocumentDto> televerser(@RequestPart("fichier") MultipartFile fichier,
            @RequestParam TypeDocument typeDocument, @RequestParam DocumentEntite entiteRattachement,
            @RequestParam Long entiteId, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.televerser(fichier, typeDocument, entiteRattachement, entiteId, request));
    }

    @PreAuthorize(DOCUMENT_ACCESS)
    @GetMapping
    public List<DocumentService.DocumentDto> lister(@RequestParam DocumentEntite entite, @RequestParam Long id, HttpServletRequest request) {
        return service.lister(entite, id, request);
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
    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentService.DocumentDto> remplacer(@PathVariable Long id,
            @RequestPart("fichier") MultipartFile fichier, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.remplacer(id, fichier, request));
    }

    @PreAuthorize(DOCUMENT_ACCESS)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiver(@PathVariable Long id, HttpServletRequest request) {
        service.archiver(id, request);
        return ResponseEntity.noContent().build();
    }
}
