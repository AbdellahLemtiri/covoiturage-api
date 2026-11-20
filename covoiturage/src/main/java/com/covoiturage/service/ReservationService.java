package com.covoiturage.service;

import com.covoiturage.dto.request.ReservationRequestDto;
import com.covoiturage.dto.response.ReservationResponseDto;

import java.util.List;

public interface ReservationService {

    ReservationResponseDto reserver(ReservationRequestDto dto);

    ReservationResponseDto consulterReservation(Long id, Long passagerId);

    List<ReservationResponseDto> findByPassager(Long passagerId);

    void annulerReservation(Long reservationId, Long passagerId);
}