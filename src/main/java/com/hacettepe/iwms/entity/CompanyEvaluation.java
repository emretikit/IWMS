package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_evaluation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "internship_id", nullable = false, unique = true)
    private Internship internship;

    @Lob
    private String internshipResultDocument;

    @Lob
    private String reportEvaluationDocument;

    @Column(length = 500)
    private String signatureFilePath;

    @Column(length = 64)
    private String signatureSha256;

    private LocalDateTime submittedAt;
}
