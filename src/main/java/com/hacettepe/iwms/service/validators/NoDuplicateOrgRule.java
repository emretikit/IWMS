package com.hacettepe.iwms.service.validators;

import com.hacettepe.iwms.entity.AcademicPeriod;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.InternshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoDuplicateOrgRule implements InternshipRuleValidator {

    private final InternshipRepository internshipRepository;

    @Override
    public void validate(Internship internship, Student student, AcademicPeriod period) {
        boolean exists = internshipRepository.existsByStudentIdAndCompanyIdAndAcademicPeriodId(
                student.getId(),
                internship.getCompany().getId(),
                period.getId()
        );
        if (exists) {
            throw new ValidationException("Validation failed: You have already applied to this company in the same academic period.");
        }
    }
}
