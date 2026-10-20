package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Candidat findByEmail(String email);
}
