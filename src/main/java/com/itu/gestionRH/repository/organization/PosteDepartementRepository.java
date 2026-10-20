package com.itu.gestionRH.repository.organization;

import com.itu.gestionRH.entity.organization.PosteDepartement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteDepartementRepository extends JpaRepository<PosteDepartement, Long> {
}
