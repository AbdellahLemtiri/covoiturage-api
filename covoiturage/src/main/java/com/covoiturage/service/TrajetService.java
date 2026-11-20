package com.covoiturage.service;

import com.covoiturage.dto.request.TrajetRequestDto;
import com.covoiturage.dto.response.ReservationResponseDto;
import com.covoiturage.dto.response.TrajetResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TrajetService {

    TrajetResponseDto create(TrajetRequestDto dto);

    TrajetResponseDto findByIdWithDetails(Long id);

    Page<TrajetResponseDto> searchTrajets(
            String depart,
            String arrivee,
            LocalDate date,
            BigDecimal prixMax,
            Integer placesMin,
            Pageable pageable
    );

    List<ReservationResponseDto> reservationsTrajet(Long trajetId, Long conducteurId);

    TrajetResponseDto terminerTrajet(Long trajetId, Long conducteurId);

    TrajetResponseDto annulerTrajet(Long trajetId, Long conducteurId);
}