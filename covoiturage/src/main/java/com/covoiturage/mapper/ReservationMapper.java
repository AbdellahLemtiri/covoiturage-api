package com.covoiturage.mapper;

import com.covoiturage.dto.response.ReservationResponseDto;
import com.covoiturage.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "passagerId", source = "entity.passager.id")
    @Mapping(target = "passagerNomComplet", expression = "java(entity.getPassager().getPrenom() + \" \" + entity.getPassager().getNom())")
    @Mapping(target = "trajetId", source = "entity.trajet.id")
    @Mapping(target = "villeMontee", source = "entity.pointMontee.ville")
    @Mapping(target = "heureMontee", source = "entity.pointMontee.heurePassage")
    @Mapping(target = "villeDescente", source = "entity.pointDescente.ville")
    @Mapping(target = "heureDescente", source = "entity.pointDescente.heurePassage")
    @Mapping(target = "placesRestantesTrajet", source = "placesRestantesTrajet")
    ReservationResponseDto toResponseDto(Reservation entity, Integer placesRestantesTrajet);
}