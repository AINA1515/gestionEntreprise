package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.OffreStatut;

@Repository
public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Long> {
    OffreEmploi findByTitre(String titre);
    Page<OffreEmploi> findByStatutOffre(OffreStatut statut, Pageable pageable);
    Page<OffreEmploi> findByDemandeId(Long demandeId, Pageable pageable);
    Page<OffreEmploi> findByStatutOffreAndDemandeId(OffreStatut statut, Long demandeId, Pageable pageable);
}
