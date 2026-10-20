package com.itu.gestionRH.repository.history;

import com.itu.gestionRH.entity.history.CandidatureRecruteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatureRecruteurRepository extends JpaRepository<CandidatureRecruteur, Long> {
}
