package com.itu.gestionRH.repository.hiring;

import com.itu.gestionRH.entity.hiring.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.ContratStatut;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    Page<Contrat> findByEmployeId(Long employeId, Pageable pageable);
    java.util.List<Contrat> findByEmployeIdOrderByDateDebutDescIdDesc(Long employeId);
    java.util.Optional<Contrat> findFirstByEmployeIdAndStatutContratInOrderByDateDebutDescIdDesc(Long employeId, java.util.Collection<ContratStatut> statuts);
    Page<Contrat> findByEmployeIdAndStatutContrat(Long employeId, ContratStatut statut, Pageable pageable);
    java.util.List<Contrat> findByEmployeIdAndStatutContratIn(Long employeId, java.util.Collection<ContratStatut> statuts);
    boolean existsByPosteId(Long posteId);
    java.util.List<Contrat> findByPosteIdAndStatutContrat(Long posteId, ContratStatut statut);
    java.util.List<Contrat> findByTypeContratAndStatutContratAndDateFinBetween(com.itu.gestionRH.entity.enums.ContratType type, ContratStatut statut, java.time.LocalDate debut, java.time.LocalDate fin);
}
