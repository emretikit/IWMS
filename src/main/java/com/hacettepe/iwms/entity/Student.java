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

    @OneToMany(mappedBy = "student")
    private List<Internship> internships;

    @OneToMany(mappedBy = "student")
    private List<CourseEnrollment> enrollments;
}
