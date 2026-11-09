package com.itu.gestionRH.repository.organization;
import com.itu.gestionRH.entity.organization.PointageJournalier;
import java.time.LocalDate; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface PointageJournalierRepository extends JpaRepository<PointageJournalier,Long>{
 List<PointageJournalier> findByEmployeIdAndDatePointageBetweenOrderByDatePointageAsc(Long id,LocalDate debut,LocalDate fin);
 List<PointageJournalier> findByEmployeIdInAndDatePointageBetweenOrderByDatePointageAsc(Collection<Long> ids,LocalDate debut,LocalDate fin);
 List<PointageJournalier> findByDatePointageBetweenOrderByDatePointageAsc(LocalDate debut,LocalDate fin);
 Optional<PointageJournalier> findByEmployeIdAndDatePointage(Long id,LocalDate date);
}
