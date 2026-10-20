package com.itu.gestionRH.repository.hiring;

import com.itu.gestionRH.entity.hiring.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
}
