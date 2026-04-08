package com.hacettepe.iwms.service.validators;

import com.hacettepe.iwms.entity.AcademicPeriod;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class MinDaysRule implements InternshipRuleValidator {

    @Override
    public void validate(Internship internship, Student student, AcademicPeriod period) {
        if (!internship.meetsMinDays(period.getMinInternshipDays())) {
            throw new ValidationException("Validation failed: Total working days must be at least " + period.getMinInternshipDays() + ".");
        }
    }
}
