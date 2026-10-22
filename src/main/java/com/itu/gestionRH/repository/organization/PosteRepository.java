package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PosteRepository extends JpaRepository<Poste, Long> {
    Poste findByCodePoste(String codePoste);
    Page<Poste> findByDepartementId(Long departementId, Pageable pageable);
    boolean existsByDepartementId(Long departementId);
}
