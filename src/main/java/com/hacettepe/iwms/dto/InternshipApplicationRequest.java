package com.hacettepe.iwms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InternshipApplicationRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long academicPeriodId;

    // @FutureOrPresent // <<<--- BU ANNOTASYON KALDIRILDI
    @NotNull
    private LocalDate startDate;

    // @FutureOrPresent // <<<--- BU ANNOTASYON KALDIRILDI
    @NotNull
    private LocalDate endDate;

    @NotNull
    @Min(1)
    private Integer totalWorkingDays;

    @NotNull
    private String lectureCode;

    // This might be null if the supervisor is not yet registered in the system
    private Long supervisorId;
}
