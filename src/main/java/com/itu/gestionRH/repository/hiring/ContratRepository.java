package com.itu.gestionRH.repository.hiring;

import com.itu.gestionRH.entity.hiring.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
}
