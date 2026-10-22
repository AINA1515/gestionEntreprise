package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.DemandeValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeValidationRepository extends JpaRepository<DemandeValidation, Long> {
    java.util.List<DemandeValidation> findByDemandeIdOrderByDateDecisionAscIdAsc(Long demandeId);
    long countByDemandeId(Long demandeId);
}
