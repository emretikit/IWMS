package com.hacettepe.iwms.service.validators;

import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.entity.AcademicPeriod;

public interface InternshipRuleValidator {
    void validate(Internship internship, Student student, AcademicPeriod period);
}
