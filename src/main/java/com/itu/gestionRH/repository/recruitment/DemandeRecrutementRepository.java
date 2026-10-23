package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.DemandeStatut;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface DemandeRecrutementRepository extends JpaRepository<DemandeRecrutement, Long> {
    DemandeRecrutement findByMotif(String motif);
    Page<DemandeRecrutement> findByStatutValidation(DemandeStatut statut, Pageable pageable);
    Page<DemandeRecrutement> findByManagerId(Long managerId, Pageable pageable);
    Page<DemandeRecrutement> findByPosteDepartementId(Long departementId, Pageable pageable);
    Page<DemandeRecrutement> findByStatutValidationAndPosteDepartementId(DemandeStatut statut, Long departementId, Pageable pageable);
    long countByManagerIdAndStatutValidation(Long managerId, DemandeStatut statut);
    long countByStatutValidation(DemandeStatut statut);

    @Query("select d from DemandeRecrutement d where d.statutValidation = com.itu.gestionRH.entity.enums.DemandeStatut.VALIDEE "
            + "and not exists (select o.id from OffreEmploi o where o.demande = d) "
            + "order by d.dateCreation desc")
    List<DemandeRecrutement> findValideesSansOffre();

    @Query("select d from DemandeRecrutement d where (:statut is null or d.statutValidation = :statut) "
            + "and (:departementId is null or d.poste.departement.id = :departementId) "
            + "and (:managerId is null or d.manager.id = :managerId) "
            + "and (:managerScope is null or d.manager.id = :managerScope) "
            + "and (:dateDebut is null or d.dateSouhaitee >= :dateDebut) "
            + "and (:dateFin is null or d.dateSouhaitee <= :dateFin) "
            + "and (:texte is null or lower(d.motif) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(d.poste.intitule) like concat(concat('%', lower(:texte)), '%') "
            + "or lower(d.poste.departement.libelle) like concat(concat('%', lower(:texte)), '%'))")
    Page<DemandeRecrutement> rechercher(@Param("statut") DemandeStatut statut,
            @Param("departementId") Long departementId, @Param("managerId") Long managerId,
            @Param("managerScope") Long managerScope, @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin, @Param("texte") String texte, Pageable pageable);
}
