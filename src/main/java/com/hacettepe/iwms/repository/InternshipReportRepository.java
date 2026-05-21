package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.InternshipReport;
import com.hacettepe.iwms.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipReportRepository extends JpaRepository<InternshipReport, Long> {
    Optional<InternshipReport> findByInternshipId(Long internshipId);
    List<InternshipReport> findBySubmissionStatus(SubmissionStatus status);
}
