package com.itu.gestionRH.repository.organization;
import com.itu.gestionRH.entity.organization.DemandeModificationDossier;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DemandeModificationDossierRepository extends JpaRepository<DemandeModificationDossier,Long> {
 List<DemandeModificationDossier> findByEmployeIdOrderByDateDemandeDesc(Long id);
 List<DemandeModificationDossier> findBySuperieurIdAndStatutOrderByDateDemandeDesc(Long id,String statut);
}
