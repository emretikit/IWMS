package com.hacettepe.iwms.service.validators;

import com.hacettepe.iwms.entity.AcademicPeriod;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class NotFirstYearRule implements InternshipRuleValidator {

    private static final List<String> ALLOWED_YEARS = Arrays.asList("2", "3", "4", "GRADUATE");

    @Override
    public void validate(Internship internship, Student student, AcademicPeriod period) {
        if (student.getCurrentYear() == null || !ALLOWED_YEARS.contains(student.getCurrentYear())) {
            throw new ValidationException("Validation failed: Students in their first year cannot apply for an internship.");
        }
    }
}
