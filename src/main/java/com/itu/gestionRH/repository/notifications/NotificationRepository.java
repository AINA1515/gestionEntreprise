package com.itu.gestionRH.repository.notifications;

import com.itu.gestionRH.entity.notifications.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByDestinataireIdOrderByEstLueAscDateCreationDesc(Long destinataireId, Pageable pageable);
    @Query("""
            select n from Notification n
            where n.destinataire.id = :destinataireId
              and (:lue is null or n.estLue = :lue)
              and (:categorie is null
                or (:categorie = 'RECRUTEMENT' and (n.codeModele like 'DEMANDE_%' or n.codeModele like 'OFFRE_%' or n.codeModele like 'CANDIDATURE_%' or n.codeModele like 'PROPOSITION_%'))
                or (:categorie = 'DOSSIER' and (n.codeModele like 'DOCUMENT_%' or n.codeModele = 'EMPLOYE_CREE'))
                or (:categorie = 'INTEGRATION' and n.codeModele like 'ONBOARDING_%')
                or (:categorie = 'CONTRAT' and n.codeModele like 'CONTRAT_%'))
            """)
    Page<Notification> rechercherPourDestinataire(@Param("destinataireId") Long destinataireId,
            @Param("lue") Boolean lue, @Param("categorie") String categorie, Pageable pageable);
    long countByDestinataireIdAndEstLueFalse(Long destinataireId);
    Optional<Notification> findByIdAndDestinataireId(Long id, Long destinataireId);
    boolean existsByCodeModeleAndEntiteRattachementAndEntiteIdAndDestinataireIdAndDateCreationAfter(
            String codeModele, String entiteRattachement, Long entiteId, Long destinataireId, LocalDateTime depuis);

    @Modifying
    @Query("update Notification n set n.estLue = true where n.destinataire.id = :destinataireId and n.estLue = false")
    int marquerToutesLues(@Param("destinataireId") Long destinataireId);

    @Query(value = "SELECT * FROM NOTIFICATION WHERE STATUT_ENVOI = 'EN_ATTENTE' AND DATE_ENVOI_PREVUE <= :maintenant AND ROWNUM <= :limite FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Notification> verrouillerEnAttente(@Param("maintenant") LocalDateTime maintenant, @Param("limite") int limite);
}
