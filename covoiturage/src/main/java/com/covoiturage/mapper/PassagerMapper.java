package com.covoiturage.mapper;

import com.covoiturage.dto.request.PassagerRequestDto;
import com.covoiturage.dto.response.PassagerResponseDto;
import com.covoiturage.entity.Passager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PassagerMapper {
    Passager toEntity(PassagerRequestDto dto);
    PassagerResponseDto toResponseDto(Passager entity);
    void updateEntityFromDto(PassagerRequestDto dto, @MappingTarget Passager entity);
}