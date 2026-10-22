package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.EmployeStatut;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Employe findByMatricule(String matricule);
    Employe findByNomAndPrenom(String nom, String prenom);
    Page<Employe> findByStatutActivite(EmployeStatut statut, Pageable pageable);
    Page<Employe> findByCandidatId(Long candidatId, Pageable pageable);
    boolean existsByCandidatId(Long candidatId);
}
