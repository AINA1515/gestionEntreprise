package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByLogin(String login);
    Utilisateur findByEmail(String email);
}
