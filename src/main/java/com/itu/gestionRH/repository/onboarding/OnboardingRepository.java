package com.itu.gestionRH.repository.onboarding;

import com.itu.gestionRH.entity.onboarding.Onboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {
    Onboarding findByEmployeId(Long employeId);
}
