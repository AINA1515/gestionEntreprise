package com.itu.gestionRH.repository.hiring;

import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropositionEmbaucheRepository extends JpaRepository<PropositionEmbauche, Long> {
    PropositionEmbauche findByCandidatureId(Long candidatureId);
}
