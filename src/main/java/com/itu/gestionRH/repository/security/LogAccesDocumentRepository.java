package com.itu.gestionRH.repository.security;

import com.itu.gestionRH.entity.security.LogAccesDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAccesDocumentRepository extends JpaRepository<LogAccesDocument, Long> {
}
