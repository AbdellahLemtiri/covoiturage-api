package com.covoiturage.mapper;

import com.covoiturage.dto.request.ConducteurRequestDto;
import com.covoiturage.dto.response.ConducteurResponseDto;
import com.covoiturage.entity.Conducteur;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConducteurMapper {
    Conducteur toEntity(ConducteurRequestDto dto);
    ConducteurResponseDto toResponseDto(Conducteur entity);
    void updateEntityFromDto(ConducteurRequestDto dto, @MappingTarget Conducteur entity);
}