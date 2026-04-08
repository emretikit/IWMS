package com.hacettepe.iwms.mapper;

import com.hacettepe.iwms.dto.InternshipReportDto;
import com.hacettepe.iwms.entity.InternshipReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InternshipReportMapper {
    @Mapping(source = "internship.id", target = "internshipId")
    InternshipReportDto toDto(InternshipReport internshipReport);

    @Mapping(source = "internshipId", target = "internship.id")
    InternshipReport toEntity(InternshipReportDto internshipReportDto);
}
