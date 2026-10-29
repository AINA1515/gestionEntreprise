package com.itu.gestionRH.repository.onboarding;

import com.itu.gestionRH.entity.onboarding.ModeleOnbTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeleOnbTacheRepository extends JpaRepository<ModeleOnbTache, Long> {
    ModeleOnbTache findByCode(String code);
    java.util.List<ModeleOnbTache> findByEstActifTrueOrderByOrdreAsc();
}
