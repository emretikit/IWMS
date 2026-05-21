package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.CompanyEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyEvaluationRepository extends JpaRepository<CompanyEvaluation, Long> {
    Optional<CompanyEvaluation> findByInternshipId(Long internshipId);
}
