package com.itu.gestionRH.repository.security;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.itu.gestionRH.entity.security.UtilisateurRole;

@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, Long> {
    UtilisateurRole findByUtilisateurIdAndRoleId(Long utilisateurId, Long roleId);
    java.util.List<UtilisateurRole> findByUtilisateurIdAndDateFinIsNullAndDateDebutLessThanEqual(Long utilisateurId, java.time.LocalDate date);

    @org.springframework.data.jpa.repository.Query("select distinct ur.utilisateur from UtilisateurRole ur where ur.role.code = :code and ur.role.estActif = true and ur.dateFin is null and ur.dateDebut <= :date and ur.utilisateur.statutCompte = com.itu.gestionRH.entity.enums.CompteStatut.ACTIF")
    java.util.List<com.itu.gestionRH.entity.security.Utilisateur> findActiveUsersByRoleCode(@org.springframework.data.repository.query.Param("code") String code,
            @org.springframework.data.repository.query.Param("date") java.time.LocalDate date);
}
