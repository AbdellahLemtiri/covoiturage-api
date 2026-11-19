package com.covoiturage.mapper;

import com.covoiturage.dto.request.VehiculeRequestDto;
import com.covoiturage.dto.response.VehiculeResponseDto;
import com.covoiturage.entity.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    @Mapping(target = "trajets", ignore = true)
    Vehicule toEntity(VehiculeRequestDto dto);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    @Mapping(target = "conducteurNomComplet", expression = "java(entity.getConducteur().getPrenom() + \" \" + entity.getConducteur().getNom())")
    VehiculeResponseDto toResponseDto(Vehicule entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    @Mapping(target = "trajets", ignore = true)
    void updateEntityFromDto(VehiculeRequestDto dto, @MappingTarget Vehicule entity);
}