package com.hacettepe.iwms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faq_entry")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String question;

    @Lob
    private String answer;

    @Column(length = 100)
    private String category;
}
