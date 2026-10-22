package com.itu.gestionRH.repository.hiring;

import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.PropositionStatut;

@Repository
public interface PropositionEmbaucheRepository extends JpaRepository<PropositionEmbauche, Long> {
    PropositionEmbauche findByCandidatureId(Long candidatureId);
    Page<PropositionEmbauche> findByStatutProposition(PropositionStatut statut, Pageable pageable);
    Page<PropositionEmbauche> findByCandidatureCandidatId(Long candidatId, Pageable pageable);
    java.util.List<PropositionEmbauche> findByStatutPropositionAndDateLimiteReponseBefore(com.itu.gestionRH.entity.enums.PropositionStatut statut, java.time.LocalDate date);
}
