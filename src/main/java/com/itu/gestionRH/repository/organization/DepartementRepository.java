package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {
    Departement findByCode(String code);
}
