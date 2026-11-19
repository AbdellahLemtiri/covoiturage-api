package com.covoiturage.service;

import com.covoiturage.dto.request.PassagerRequestDto;
import com.covoiturage.dto.response.PassagerResponseDto;
import com.covoiturage.dto.response.ReservationResponseDto;

import java.util.List;

public interface PassagerService {
    PassagerResponseDto creat(PassagerRequestDto dto);
    PassagerResponseDto findById(Long id);
    List<PassagerResponseDto> findALl();
    PassagerResponseDto update(Long id, PassagerRequestDto dto);
    void delete(Long id);
    PassagerResponseDto searchByEmail(String email);
    List<ReservationResponseDto> findReservationsByPassagerId(Long passagerId);
}