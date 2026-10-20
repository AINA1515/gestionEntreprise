package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.EvaluationCritere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationCritereRepository extends JpaRepository<EvaluationCritere, Long> {
}
