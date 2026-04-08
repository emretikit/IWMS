package com.hacettepe.iwms.mapper;

import com.hacettepe.iwms.dto.InternshipResponseDto;
import com.hacettepe.iwms.entity.Internship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InternshipMapper {

    @Mapping(source = "student.user.name", target = "studentName")
    @Mapping(source = "company.name", target = "companyName")
    @Mapping(source = "internship", target = "hasReport", qualifiedByName = "hasReport")
    @Mapping(source = "internship", target = "hasEvaluation", qualifiedByName = "hasEvaluation")
    InternshipResponseDto toDto(Internship internship);

    List<InternshipResponseDto> toDtoList(List<Internship> internships);

    @Named("hasReport")
    default boolean hasReport(Internship internship) {
        return internship.getReport() != null;
    }

    @Named("hasEvaluation")
    default boolean hasEvaluation(Internship internship) {
        return internship.getEvaluation() != null;
    }
}
