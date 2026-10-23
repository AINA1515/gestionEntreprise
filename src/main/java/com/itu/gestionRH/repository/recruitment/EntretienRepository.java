package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, Long> {
    java.util.List<Entretien> findByIntervenantIdOrderByDateEntretienAsc(Long intervenantId);
    java.util.List<Entretien> findByCandidatureOffreDemandeManagerIdOrderByDateEntretienAsc(Long managerId);
}
