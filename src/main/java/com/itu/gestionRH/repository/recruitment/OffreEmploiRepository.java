package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.OffreStatut;
import com.itu.gestionRH.entity.enums.ContratType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Long> {
    OffreEmploi findByTitre(String titre);
    Page<OffreEmploi> findByStatutOffre(OffreStatut statut, Pageable pageable);
    Page<OffreEmploi> findByDemandeId(Long demandeId, Pageable pageable);
    Page<OffreEmploi> findByStatutOffreAndDemandeId(OffreStatut statut, Long demandeId, Pageable pageable);
    long countByDemandeIdAndStatutOffreIn(Long demandeId, Collection<OffreStatut> statuts);
    boolean existsByDemandeId(Long demandeId);
    List<OffreEmploi> findByStatutOffreAndDateLimiteCandidatureBefore(OffreStatut statut, LocalDate date);

    @Query("select o from OffreEmploi o where "
            + "(:statut is null or o.statutOffre = :statut) "
            + "and (:departementId is null or o.demande.poste.departement.id = :departementId) "
            + "and (:typeContrat is null or o.typeContrat = :typeContrat) "
            + "and (:texte is null or lower(o.titre) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(coalesce(o.descriptionPoste, '')) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(coalesce(o.profilRecherche, '')) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(o.demande.poste.intitule) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(o.demande.poste.departement.libelle) like concat(concat('%', lower(:texte)), '%'))")
    Page<OffreEmploi> rechercherInterne(@Param("statut") OffreStatut statut,
            @Param("dateReference") LocalDate dateReference,
            @Param("departementId") Long departementId, @Param("typeContrat") ContratType typeContrat,
            @Param("texte") String texte, Pageable pageable);

    @Query("select o from OffreEmploi o where o.statutOffre = com.itu.gestionRH.entity.enums.OffreStatut.PUBLIEE "
            + "and o.dateCloture is null "
            + "and (o.dateLimiteCandidature is null or o.dateLimiteCandidature >= :dateReference) "
            + "and (:departementId is null or o.demande.poste.departement.id = :departementId) "
            + "and (:typeContrat is null or o.typeContrat = :typeContrat) "
            + "and (:texte is null or lower(o.titre) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(coalesce(o.descriptionPoste, '')) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(coalesce(o.profilRecherche, '')) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(o.demande.poste.intitule) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(o.demande.poste.departement.libelle) like concat(concat('%', lower(:texte)), '%'))")
    Page<OffreEmploi> rechercherPubliques(@Param("dateReference") LocalDate dateReference,
            @Param("departementId") Long departementId, @Param("typeContrat") ContratType typeContrat,
            @Param("texte") String texte, Pageable pageable);
}
