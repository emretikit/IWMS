package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.AcademicAdvisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicAdvisorRepository extends JpaRepository<AcademicAdvisor, Long> {
}
