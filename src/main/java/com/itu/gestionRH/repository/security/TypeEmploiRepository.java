package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.TypeEmploi;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeEmploiRepository extends JpaRepository<TypeEmploi, String> {
    List<TypeEmploi> findByEstActifTrueOrderByOrdreAsc();
    boolean existsByCodeAndEstActifTrue(String code);
}
