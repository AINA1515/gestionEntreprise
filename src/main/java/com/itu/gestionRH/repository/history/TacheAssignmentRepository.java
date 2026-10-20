package com.itu.gestionRH.repository.history;

import com.itu.gestionRH.entity.history.TacheAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TacheAssignmentRepository extends JpaRepository<TacheAssignment, Long> {
}
