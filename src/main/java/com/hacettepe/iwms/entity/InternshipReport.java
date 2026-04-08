package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "internship_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternshipReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "internship_id", nullable = false, unique = true)
    private Internship internship;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Lob
    private String templateContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", length = 20)
    private SubmissionStatus submissionStatus;

    @Column(name = "is_draft", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean draft;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method
    public boolean isLate(AcademicPeriod period) {
        if (submittedAt == null || period == null || period.getSubmissionDeadline() == null) {
            return false;
        }
        return submittedAt.toLocalDate().isAfter(period.getSubmissionDeadline());
    }
}
