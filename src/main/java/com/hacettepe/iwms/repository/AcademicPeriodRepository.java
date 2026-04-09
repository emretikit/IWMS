package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.AcademicPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, Long> {
    Optional<AcademicPeriod> findByIsActiveTrue();
    @Query("select ap from AcademicPeriod ap where ap.isActive = true order by ap.year desc")
    List<AcademicPeriod> findAllByIsActiveTrueOrderByYearDesc();
    List<AcademicPeriod> findByYear(int year);
}
