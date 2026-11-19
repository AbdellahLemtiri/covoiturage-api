package com.covoiturage.service;

import com.covoiturage.dto.request.ConducteurRequestDto;
import com.covoiturage.dto.response.ConducteurResponseDto;
import com.covoiturage.entity.Conducteur;

import java.util.List;

public interface ConducteurService {

    ConducteurResponseDto creat(ConducteurRequestDto conducteurRequestDto);

    ConducteurResponseDto findById(Long id);

    List<ConducteurResponseDto> findAll();

    void delete(Long id);

    ConducteurResponseDto update(Long id ,ConducteurRequestDto conducteur);
    List<ConducteurResponseDto> searchByEmailAndNom(String string);

}
