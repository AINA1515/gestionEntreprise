package com.itu.gestionRH.repository.documents;

import com.itu.gestionRH.entity.documents.DocumentObligatoire;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentObligatoireRepository extends JpaRepository<DocumentObligatoire, Long> {
    List<DocumentObligatoire> findByEstActifTrueOrderByOrdreAscIdAsc();
    List<DocumentObligatoire> findAllByOrderByOrdreAscIdAsc();
}
