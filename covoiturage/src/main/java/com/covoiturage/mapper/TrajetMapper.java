package com.covoiturage.mapper;

import com.covoiturage.dto.request.PointArretRequestDto;
import com.covoiturage.dto.response.PointArretResponseDto;
import com.covoiturage.dto.response.TrajetResponseDto;
import com.covoiturage.entity.PointArret;
import com.covoiturage.entity.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TrajetMapper {

    PointArret toPointArretEntity(PointArretRequestDto dto);

    PointArretResponseDto toPointArretResponseDto(PointArret entity);

    @Mapping(target = "conducteurId", source = "entity.conducteur.id")
    @Mapping(target = "conducteurNomComplet", expression = "java(entity.getConducteur().getPrenom() + \" \" + entity.getConducteur().getNom())")
    @Mapping(target = "vehiculeId", source = "entity.vehicule.id")
    @Mapping(target = "vehiculeModele", expression = "java(entity.getVehicule().getMarque() + \" \" + entity.getVehicule().getModele())")
    @Mapping(target = "placesRestantes", source = "placesRestantes")
    @Mapping(target = "prixSegment", source = "prixSegment")
    TrajetResponseDto toResponseDto(Trajet entity, Integer placesRestantes, BigDecimal prixSegment);
}