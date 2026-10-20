package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeRecrutementRepository extends JpaRepository<DemandeRecrutement, Long> {
    DemandeRecrutement findByMotif(String motif);
}
