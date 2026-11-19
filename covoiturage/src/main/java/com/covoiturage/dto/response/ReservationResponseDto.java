package com.covoiturage.dto.response;

import com.covoiturage.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponseDto(
        Long id,
        Long passagerId,
        String passagerNomComplet,
        Long trajetId,
        String villeMontee,
        LocalDateTime heureMontee,
        String villeDescente,
        LocalDateTime heureDescente,
        Integer nombrePlaces,
        BigDecimal montantTotal,
        Integer placesRestantesTrajet,
        LocalDateTime dateReservation,
        StatutReservation statut
) {}