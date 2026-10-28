package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.itu.gestionRH.entity.enums.EmployeStatut;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Employe findByMatricule(String matricule);
    Employe findByNomAndPrenom(String nom, String prenom);
    Page<Employe> findByStatutActivite(EmployeStatut statut, Pageable pageable);
    Page<Employe> findByCandidatId(Long candidatId, Pageable pageable);
    boolean existsByCandidatId(Long candidatId);
    boolean existsByEmailProfessionnelIgnoreCase(String emailProfessionnel);
    @Query("select distinct e from Employe e where (:statut is null or e.statutActivite = :statut) "
        + "and (:texte is null or lower(e.nom) like concat(concat('%', lower(:texte)), '%') "
        + "or lower(e.prenom) like concat(concat('%', lower(:texte)), '%') "
        + "or lower(e.matricule) like concat(concat('%', lower(:texte)), '%') "
        + "or lower(e.emailProfessionnel) like concat(concat('%', lower(:texte)), '%')) "
        + "and (:departementId is null or exists (select a.id from Affectation a where a.employe = e "
        + "and a.dateFin is null and a.poste.departement.id = :departementId))")
    Page<Employe> rechercher(@Param("departementId") Long departementId, @Param("statut") EmployeStatut statut,
            @Param("texte") String texte, Pageable pageable);
}
