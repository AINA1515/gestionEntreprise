package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.CandidatureStatut;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    Candidature findByOffreIdAndCandidatId(Long offreId, Long candidatId);
    Page<Candidature> findByStatutCandidature(CandidatureStatut statut, Pageable pageable);
    Page<Candidature> findByOffreId(Long offreId, Pageable pageable);
    Page<Candidature> findByCandidatId(Long candidatId, Pageable pageable);
    Page<Candidature> findByOffreIdAndStatutCandidature(Long offreId, CandidatureStatut statut, Pageable pageable);
    long countByOffreId(Long offreId);
}
