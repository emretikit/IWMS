package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "internship_supervisor")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternshipSupervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 100)
    private String title;

    @Column(name = "company_email", unique = true, length = 150)
    private String companyEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "engineer_type", length = 50)
    private EngineerType engineerType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "supervisor")
    private List<Internship> supervisedInternships;
}
