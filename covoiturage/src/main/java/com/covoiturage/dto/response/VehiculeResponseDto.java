package com.covoiturage.dto.response;

public record VehiculeResponseDto(
        Long id,
        String marque,
        String modele,
        String immatriculation,
        String couleur,
        Integer capacite,
        Long conducteurId,
        String conducteurNomComplet
) {}