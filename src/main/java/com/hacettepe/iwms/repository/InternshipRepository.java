package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.InternshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    List<Internship> findByStudentId(Long studentId);
    List<Internship> findByAcademicPeriodId(Long periodId);
    boolean existsByStudentIdAndCompanyIdAndAcademicPeriodId(Long studentId, Long companyId, Long periodId);
    boolean existsByStudentIdAndCompanyId(Long studentId, Long companyId);
    List<Internship> findByStatus(InternshipStatus status);
    List<Internship> findByStatusIn(List<InternshipStatus> statuses);
    List<Internship> findByStudentUserId(Long userId);
    List<Internship> findBySupervisorCompanyEmailIgnoreCase(String companyEmail);
    long countByAcademicPeriodId(Long periodId);
    long countByStudentId(Long studentId);
    List<Internship> findByCompanyId(Long companyId);
}
