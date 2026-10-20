package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Long> {
    OffreEmploi findByTitre(String titre);
}
