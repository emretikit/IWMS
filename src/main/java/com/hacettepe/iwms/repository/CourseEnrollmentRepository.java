package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    long countByStudentId(Long studentId);
}
