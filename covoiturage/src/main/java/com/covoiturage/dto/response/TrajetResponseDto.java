package com.covoiturage.dto.response;

import com.covoiturage.enums.StatutTrajet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TrajetResponseDto(
        Long id,
        Long conducteurId,
        String conducteurNomComplet,
        Long vehiculeId,
        String vehiculeModele,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        Integer placesProposees,
        Integer placesRestantes,
        BigDecimal prixSegment,
        StatutTrajet statut,
        List<PointArretResponseDto> pointsArret
) {}