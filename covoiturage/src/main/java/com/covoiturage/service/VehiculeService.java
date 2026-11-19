package com.covoiturage.service;

import com.covoiturage.dto.request.VehiculeRequestDto;
import com.covoiturage.dto.response.VehiculeResponseDto;

import java.util.List;

public interface VehiculeService {
    VehiculeResponseDto creat(VehiculeRequestDto dto);
    VehiculeResponseDto findById(Long id);
    List<VehiculeResponseDto> findALl();
    VehiculeResponseDto update(Long id, VehiculeRequestDto dto);
    void delete(Long id);
    List<VehiculeResponseDto> findByConducteurId(Long conducteurId);
}