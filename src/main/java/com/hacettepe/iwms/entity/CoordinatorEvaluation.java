package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coordinator_evaluation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatorEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "internship_id", nullable = false, unique = true)
    private Internship internship;

    @ManyToOne
    @JoinColumn(name = "coordinator_id", nullable = false)
    private InternshipCoordinator coordinator;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EvaluationResult result = EvaluationResult.PENDING;

    @Lob
    private String feedback;

    @Column(name = "documents_complete", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean documentsComplete = false;

    @Column(name = "rules_compliant", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean rulesCompliant = false;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;
}
