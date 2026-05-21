package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.InternshipCoordinator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InternshipCoordinatorRepository extends JpaRepository<InternshipCoordinator, Long> {
    Optional<InternshipCoordinator> findByUserId(Long userId);
}
