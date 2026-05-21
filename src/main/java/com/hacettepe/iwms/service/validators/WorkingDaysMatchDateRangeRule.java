package com.hacettepe.iwms.service.validators;

import com.hacettepe.iwms.entity.AcademicPeriod;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class WorkingDaysMatchDateRangeRule implements InternshipRuleValidator {

    @Override
    public void validate(Internship internship, Student student, AcademicPeriod period) {
        if (internship.getStartDate() == null || internship.getEndDate() == null || internship.getTotalWorkingDays() == null) {
            return;
        }

        long expectedDays = ChronoUnit.DAYS.between(internship.getStartDate(), internship.getEndDate()) + 1;
        if (expectedDays != internship.getTotalWorkingDays()) {
            throw new ValidationException("Total working days must match the number of days between start and end date.");
        }
    }
}
