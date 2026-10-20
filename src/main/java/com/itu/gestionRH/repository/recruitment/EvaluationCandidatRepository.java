package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.EvaluationCandidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationCandidatRepository extends JpaRepository<EvaluationCandidat, Long> {
}
