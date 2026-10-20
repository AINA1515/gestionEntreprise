package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    Candidature findByOffreIdAndCandidatId(Long offreId, Long candidatId);
}
