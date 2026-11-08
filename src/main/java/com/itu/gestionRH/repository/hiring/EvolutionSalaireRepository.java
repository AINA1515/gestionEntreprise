package com.itu.gestionRH.repository.hiring;

import com.itu.gestionRH.entity.hiring.EvolutionSalaire;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvolutionSalaireRepository extends JpaRepository<EvolutionSalaire, Long> {
    List<EvolutionSalaire> findByEmployeIdOrderByDateEffetAscIdAsc(Long employeId);
}
