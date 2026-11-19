package com.covoiturage.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TrajetRequestDto(
        @NotNull(message = "L'identifiant du conducteur est obligatoire")
        Long conducteurId,

        @NotNull(message = "L'identifiant du véhicule est obligatoire")
        Long vehiculeId,

        @NotNull(message = "Le nombre de places proposées est obligatoire")
        @Min(value = 1, message = "Il faut proposer au moins 1 place")
        Integer placesProposees,

        @NotNull(message = "La liste des points d'arrêt est obligatoire")
        @Size(min = 2, message = "Un trajet doit contenir au moins 2 points d'arrêt (départ et arrivée)")
        @Valid
        List<PointArretRequestDto> pointsArret
) {}