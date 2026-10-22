package com.itu.gestionRH.repository.recruitment;

import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.DemandeStatut;

@Repository
public interface DemandeRecrutementRepository extends JpaRepository<DemandeRecrutement, Long> {
    DemandeRecrutement findByMotif(String motif);
    Page<DemandeRecrutement> findByStatutValidation(DemandeStatut statut, Pageable pageable);
    Page<DemandeRecrutement> findByManagerId(Long managerId, Pageable pageable);
    Page<DemandeRecrutement> findByPosteDepartementId(Long departementId, Pageable pageable);
    Page<DemandeRecrutement> findByStatutValidationAndPosteDepartementId(DemandeStatut statut, Long departementId, Pageable pageable);
}
