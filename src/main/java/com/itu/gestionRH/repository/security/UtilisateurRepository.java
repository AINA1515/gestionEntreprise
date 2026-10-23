package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByLogin(String login);
    Utilisateur findByEmail(String email);
    java.util.Optional<Utilisateur> findByLoginIgnoreCaseOrEmailIgnoreCase(String login, String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByLoginIgnoreCase(String login);
    @Query("select u from Utilisateur u where u.candidat.id = :candidatId")
    java.util.Optional<Utilisateur> findByCandidatId(@Param("candidatId") Long candidatId);

    @Query("select u from Utilisateur u where u.employe.id = :employeId")
    java.util.Optional<Utilisateur> findByEmployeId(@Param("employeId") Long employeId);
}
