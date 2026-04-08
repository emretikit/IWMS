package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "supervisor_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @Column(name = "supervisor_email", length = 150)
    private String supervisorEmail;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(name = "verification_code", length = 10)
    private String verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TokenStatus status = TokenStatus.PENDING;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "attempt_count", columnDefinition = "INT DEFAULT 0")
    private int attemptCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isVerified() {
        return this.status == TokenStatus.VERIFIED;
    }

    public boolean canAttempt() {
        return this.attemptCount < 5;
    }
}
