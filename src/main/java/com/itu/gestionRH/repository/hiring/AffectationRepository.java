package com.itu.gestionRH.repository.hiring;

import com.itu.gestionRH.entity.hiring.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    boolean existsByEmployeIdAndSuperieurIdAndDateFinIsNull(Long employeId, Long superieurId);
    java.util.Optional<Affectation> findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(Long employeId);
    java.util.List<Affectation> findBySuperieurIdAndDateFinIsNull(Long superieurId);
    boolean existsByPosteIdAndDateFinIsNull(Long posteId);
    java.util.List<Affectation> findByPosteIdAndDateFinIsNull(Long posteId);
}
