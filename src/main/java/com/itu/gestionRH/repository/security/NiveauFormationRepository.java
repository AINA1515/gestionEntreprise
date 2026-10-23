package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.NiveauFormation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NiveauFormationRepository extends JpaRepository<NiveauFormation, String> {
    List<NiveauFormation> findByEstActifTrueOrderByOrdreAsc();
    boolean existsByCodeAndEstActifTrue(String code);
}
