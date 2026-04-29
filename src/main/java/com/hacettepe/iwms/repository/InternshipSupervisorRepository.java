package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.InternshipSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipSupervisorRepository extends JpaRepository<InternshipSupervisor, Long> {
    List<InternshipSupervisor> findByCompanyId(Long companyId);
    Optional<InternshipSupervisor> findByCompanyEmailIgnoreCase(String companyEmail);
}
