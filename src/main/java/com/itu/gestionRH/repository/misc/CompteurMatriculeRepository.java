package com.itu.gestionRH.repository.misc;

import com.itu.gestionRH.entity.misc.CompteurMatricule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompteurMatriculeRepository extends JpaRepository<CompteurMatricule, CompteurMatricule.CompteurMatriculeId> {
}
