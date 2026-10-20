package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteRepository extends JpaRepository<Poste, Long> {
    Poste findByCodePoste(String codePoste);
}
