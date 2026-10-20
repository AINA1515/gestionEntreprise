package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.RoleRh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRhRepository extends JpaRepository<RoleRh, Long> {
    RoleRh findByCode(String code);
}
