package com.itu.gestionRH.repository.security;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.itu.gestionRH.entity.security.UtilisateurRole;

@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, Long> {
    UtilisateurRole findByUtilisateurIdAndRoleId(Long utilisateurId, Long roleId);
    java.util.List<UtilisateurRole> findByUtilisateurIdAndDateFinIsNullAndDateDebutLessThanEqual(Long utilisateurId, java.time.LocalDate date);
}
