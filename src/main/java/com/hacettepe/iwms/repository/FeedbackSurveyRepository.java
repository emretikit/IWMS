package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.FeedbackSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackSurveyRepository extends JpaRepository<FeedbackSurvey, Long> {
    List<FeedbackSurvey> findBySubmittedById(Long userId);
}
