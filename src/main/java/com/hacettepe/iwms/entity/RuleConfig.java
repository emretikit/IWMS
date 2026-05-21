package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rule_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String ruleKey;

    @Column(nullable = false, length = 255)
    private String ruleValue;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    private LocalDateTime updatedAt;
}
