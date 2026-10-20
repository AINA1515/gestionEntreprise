package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Permission findByCode(String code);
}
