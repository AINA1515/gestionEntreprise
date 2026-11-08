package com.itu.gestionRH.repository.documents;

import com.itu.gestionRH.entity.documents.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    /** Documents actifs d'un type qui expirent dans la fenêtre donnée (alertes d'expiration). */
    @Query("select d from Document d where d.statutDocument = com.itu.gestionRH.entity.enums.DocumentStatut.ACTIF "
            + "and d.entiteRattachement = com.itu.gestionRH.entity.enums.DocumentEntite.EMPLOYE and d.typeDocument = :type "
            + "and d.dateExpiration between :debut and :fin")
    List<Document> expirantEntre(@Param("type") TypeDocument type, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    List<Document> findByEntiteRattachementAndEntiteIdInAndStatutDocument(DocumentEntite entite, Collection<Long> entiteIds, DocumentStatut statut);
}
