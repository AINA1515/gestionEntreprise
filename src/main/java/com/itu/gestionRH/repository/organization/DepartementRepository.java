package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {
    Departement findByCode(String code);
    java.util.List<Departement> findByResponsableIdAndEstActifTrue(Long responsableId);
    Page<Departement> findByEstActif(Boolean estActif, Pageable pageable);
    boolean existsByDepartementParentId(Long id);
}
