package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.LogConnexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogConnexionRepository extends JpaRepository<LogConnexion, Long> {
}
