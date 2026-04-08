package com.hacettepe.iwms.mapper;

import com.hacettepe.iwms.dto.CompanyResponseDto;
import com.hacettepe.iwms.dto.InternshipSupervisorDto;
import com.hacettepe.iwms.entity.Company;
import com.hacettepe.iwms.entity.InternshipSupervisor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    CompanyResponseDto toCompanyResponseDto(Company company);

    List<CompanyResponseDto> toCompanyResponseDtoList(List<Company> companies);

    InternshipSupervisorDto toInternshipSupervisorDto(InternshipSupervisor supervisor);

    List<InternshipSupervisorDto> toInternshipSupervisorDtoList(List<InternshipSupervisor> supervisors);
}
