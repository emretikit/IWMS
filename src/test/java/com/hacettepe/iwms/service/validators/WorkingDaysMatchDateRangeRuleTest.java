package com.hacettepe.iwms.service.validators;

import com.hacettepe.iwms.entity.AcademicPeriod;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkingDaysMatchDateRangeRuleTest {

    private final WorkingDaysMatchDateRangeRule rule = new WorkingDaysMatchDateRangeRule();

    @Test
    void shouldRejectWhenRangeAndWorkingDaysDoNotMatch() {
        Internship internship = Internship.builder()
                .startDate(LocalDate.of(2026, 4, 10))
                .endDate(LocalDate.of(2026, 4, 30))
                .totalWorkingDays(20)
                .build();

        assertThrows(ValidationException.class, () -> rule.validate(internship, new Student(), new AcademicPeriod()));
    }

    @Test
    void shouldAllowWhenRangeAndWorkingDaysMatch() {
        Internship internship = Internship.builder()
                .startDate(LocalDate.of(2026, 4, 10))
                .endDate(LocalDate.of(2026, 4, 30))
                .totalWorkingDays(21)
                .build();

        assertDoesNotThrow(() -> rule.validate(internship, new Student(), new AcademicPeriod()));
    }
}
