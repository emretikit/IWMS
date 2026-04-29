package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.FaqEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaqEntryRepository extends JpaRepository<FaqEntry, Long> {
    List<FaqEntry> findByQuestionContainingIgnoreCase(String query);
    Optional<FaqEntry> findByQuestionIgnoreCase(String question);
}
