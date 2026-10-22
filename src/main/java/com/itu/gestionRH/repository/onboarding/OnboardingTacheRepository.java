package com.itu.gestionRH.repository.onboarding;

import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.itu.gestionRH.entity.enums.ServiceResponsable;
import com.itu.gestionRH.entity.enums.TacheStatut;

@Repository
public interface OnboardingTacheRepository extends JpaRepository<OnboardingTache, Long> {
    Page<OnboardingTache> findByStatutTache(TacheStatut statut, Pageable pageable);
    Page<OnboardingTache> findByServiceResponsable(ServiceResponsable service, Pageable pageable);
    Page<OnboardingTache> findByAssigneeAId(Long assigneeId, Pageable pageable);
    Page<OnboardingTache> findByAssigneeAIdAndStatutTache(Long assigneeId, TacheStatut statut, Pageable pageable);
    java.util.List<OnboardingTache> findByDateLimiteBeforeAndStatutTacheIn(java.time.LocalDate date, java.util.Collection<TacheStatut> statuts);
}
