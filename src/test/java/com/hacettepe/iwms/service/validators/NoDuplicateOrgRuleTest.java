package com.hacettepe.iwms.service.validators;

import com.hacettepe.iwms.entity.AcademicPeriod;
import com.hacettepe.iwms.entity.Company;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.InternshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoDuplicateOrgRuleTest {

    @Mock
    private InternshipRepository internshipRepository;

    @Test
    void shouldRejectWhenStudentAlreadyHasInternshipAtSameCompany() {
        NoDuplicateOrgRule rule = new NoDuplicateOrgRule(internshipRepository);
        Student student = Student.builder().id(4L).build();
        Company company = Company.builder().id(9L).build();
        Internship internship = Internship.builder().company(company).build();

        when(internshipRepository.existsByStudentIdAndCompanyId(4L, 9L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> rule.validate(internship, student, new AcademicPeriod()));
    }

    @Test
    void shouldAllowWhenStudentHasNoPreviousInternshipAtSameCompany() {
        NoDuplicateOrgRule rule = new NoDuplicateOrgRule(internshipRepository);
        Student student = Student.builder().id(4L).build();
        Company company = Company.builder().id(9L).build();
        Internship internship = Internship.builder().company(company).build();

        when(internshipRepository.existsByStudentIdAndCompanyId(4L, 9L)).thenReturn(false);

        assertDoesNotThrow(() -> rule.validate(internship, student, new AcademicPeriod()));
    }
}
