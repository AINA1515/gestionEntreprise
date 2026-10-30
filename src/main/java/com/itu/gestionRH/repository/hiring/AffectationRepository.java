package com.itu.gestionRH.repository.hiring;

import com.itu.gestionRH.entity.hiring.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    boolean existsByEmployeIdAndSuperieurIdAndDateFinIsNull(Long employeId, Long superieurId);
    java.util.Optional<Affectation> findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(Long employeId);
    java.util.Optional<Affectation> findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDescIdDesc(Long employeId);
    java.util.Optional<Affectation> findFirstByEmployeIdAndPosteIdAndDateFinIsNullOrderByDateDebutDesc(Long employeId, Long posteId);
    java.util.List<Affectation> findBySuperieurIdAndDateFinIsNull(Long superieurId);
    boolean existsByPosteIdAndDateFinIsNull(Long posteId);
    java.util.List<Affectation> findByPosteIdAndDateFinIsNull(Long posteId);
    @Query("select a.employe.id from Affectation a where a.dateFin is null and a.poste.departement.id = :departementId")
    java.util.List<Long> findEmployeIdsActifsByDepartementId(@Param("departementId") Long departementId);
    java.util.List<Affectation> findByEmployeIdOrderByDateDebutDescIdDesc(Long employeId);
}
