package com.covoiturage.dto.response;

public record PassagerResponseDto(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone
) {}