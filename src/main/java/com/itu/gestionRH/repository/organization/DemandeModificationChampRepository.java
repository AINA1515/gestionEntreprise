package com.itu.gestionRH.repository.organization;
import com.itu.gestionRH.entity.organization.DemandeModificationChamp; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface DemandeModificationChampRepository extends JpaRepository<DemandeModificationChamp,Long>{ List<DemandeModificationChamp> findByDemandeId(Long demandeId); }
