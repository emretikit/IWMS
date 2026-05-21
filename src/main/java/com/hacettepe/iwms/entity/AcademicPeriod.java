package com.hacettepe.iwms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "academic_period")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "semester_type", length = 20)
    private SemesterType semesterType;

    private int year;

    @Column(name = "submission_deadline")
    private LocalDate submissionDeadline;

    @Column(name = "late_deadline")
    private LocalDate lateDeadline;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isActive = false;

    @Column(name = "min_internship_days", columnDefinition = "INT DEFAULT 20")
    private int minInternshipDays = 20;

    @Column(name = "max_orgs_per_period", columnDefinition = "INT DEFAULT 1")
    private int maxOrgsPerPeriod = 1;

    @OneToMany(mappedBy = "academicPeriod")
    @JsonIgnore
    private List<CourseEnrollment> enrollments;

    @OneToMany(mappedBy = "academicPeriod")
    @JsonIgnore
    private List<Internship> internships;

    // Helper methods
    public boolean isSubmissionOpen() {
        return LocalDate.now().isBefore(submissionDeadline) || LocalDate.now().isEqual(submissionDeadline);
    }

    public boolean isLateSubmissionOpen() {
        return LocalDate.now().isAfter(submissionDeadline) && (LocalDate.now().isBefore(lateDeadline) || LocalDate.now().isEqual(lateDeadline));
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(lateDeadline);
    }
}
