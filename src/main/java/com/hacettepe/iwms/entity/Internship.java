package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "internship")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_enrollment_id")
    private CourseEnrollment courseEnrollment;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private InternshipSupervisor supervisor;

    @ManyToOne
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriod academicPeriod;

    @ManyToOne
    @JoinColumn(name = "coordinator_id")
    private InternshipCoordinator coordinator;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_working_days")
    private Integer totalWorkingDays;

    @Column(name = "absent_days", columnDefinition = "INT DEFAULT 0")
    private int absentDays = 0;

    @Column(name = "is_multidisciplinary", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isMultidisciplinary = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private InternshipStatus status = InternshipStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "internship", cascade = CascadeType.ALL)
    private InternshipReport report;

    @OneToOne(mappedBy = "internship", cascade = CascadeType.ALL)
    private CoordinatorEvaluation evaluation;

    // Helper methods
    public boolean isAbsenceCompliant() {
        if (totalWorkingDays == null || totalWorkingDays == 0) {
            return true;
        }
        return absentDays <= (0.20 * totalWorkingDays);
    }

    public boolean meetsMinDays(int minDays) {
        if (totalWorkingDays == null) {
            return false;
        }
        return totalWorkingDays >= minDays;
    }
}
