package com.itu.gestionRH.repository.documents;

import com.itu.gestionRH.entity.documents.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.DocumentStatut;
import com.itu.gestionRH.entity.enums.TypeDocument;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEntiteRattachementAndEntiteIdAndStatutDocumentNotOrderByNumeroVersionDesc(
            DocumentEntite entite, Long entiteId, DocumentStatut statutDocument);
    Optional<Document> findFirstByEntiteRattachementAndEntiteIdAndTypeDocumentAndStatutDocumentOrderByNumeroVersionDesc(
            DocumentEntite entite, Long entiteId, TypeDocument typeDocument, DocumentStatut statutDocument);
}
