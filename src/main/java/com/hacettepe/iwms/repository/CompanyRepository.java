package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.Company;
import com.hacettepe.iwms.entity.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByApprovalStatus(ApprovalStatus status);
    Optional<Company> findByNameIgnoreCase(String name);
}
