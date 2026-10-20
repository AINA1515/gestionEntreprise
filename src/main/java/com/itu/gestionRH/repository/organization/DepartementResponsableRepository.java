package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.DepartementResponsable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartementResponsableRepository extends JpaRepository<DepartementResponsable, Long> {
}
