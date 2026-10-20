package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Employe findByMatricule(String matricule);
    Employe findByNomAndPrenom(String nom, String prenom);
}
