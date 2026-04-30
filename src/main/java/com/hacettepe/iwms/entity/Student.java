package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "student")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @Column(name = "student_number", unique = true, nullable = false, length = 20)
    private String studentNumber;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private AcademicAdvisor advisor;

    @Column(name = "current_year", length = 10)
    private String currentYear;

    @Column(length = 100)
    private String department;

    @Column(length = 150)
    private String faculty;

    @Column(name = "advisor_name", length = 150)
    private String advisorName;

    @Column(name = "education_type", length = 50)
    private String educationType;

    @Column(name = "registration_date", length = 50)
    private String registrationDate;

    @Column(name = "grade_note", length = 50)
    private String gradeNote;

    @Column(name = "agno")
    private Double agno;

    @OneToMany(mappedBy = "student")
    private List<Internship> internships;

    @OneToMany(mappedBy = "student")
    private List<CourseEnrollment> enrollments;
}
