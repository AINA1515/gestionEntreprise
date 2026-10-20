package com.itu.gestionRH.repository.onboarding;

import com.itu.gestionRH.entity.onboarding.OnboardingTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnboardingTacheRepository extends JpaRepository<OnboardingTache, Long> {
}
