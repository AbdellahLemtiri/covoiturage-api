package com.covoiturage.service.impl;

import com.covoiturage.dto.request.ConducteurRequestDto;
import com.covoiturage.dto.response.ConducteurResponseDto;
import com.covoiturage.entity.Conducteur;
import com.covoiturage.exception.BusinessRuleException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.ConducteurMapper;
import com.covoiturage.repository.ConducteurRepository;
import com.covoiturage.service.ConducteurService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurMapper conducteurMapper;
    private final ConducteurRepository conducteurRepository;

    @Override
    public ConducteurResponseDto creat(ConducteurRequestDto dto) {
        if (conducteurRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new BusinessRuleException("L'email est déjà utilisé !, merci de changer");
        }
        if (conducteurRepository.existsByNumeroPermisIgnoreCase(dto.numeroPermis())) {
            throw new BusinessRuleException("Le numéro de permis est déjà utilisé !, merci de changer");
        }

        Conducteur conducteur = conducteurMapper.toEntity(dto);
        Conducteur savedConducteur = conducteurRepository.save(conducteur);
        return conducteurMapper.toResponseDto(savedConducteur);
    }

    @Override
    @Transactional(readOnly = true)
    public ConducteurResponseDto findById(Long id) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + id));
        return conducteurMapper.toResponseDto(conducteur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponseDto> findAll() {
        return conducteurRepository.findAll().stream()
                .map(conducteurMapper::toResponseDto)
                .toList();
    }

    @Override
    public ConducteurResponseDto update(Long id, ConducteurRequestDto dto) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + id));


        boolean emailExists = conducteurRepository.findAll().stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(dto.email()) && !c.getId().equals(id));
        if (emailExists) {
            throw new BusinessRuleException("L'email est déjà utilisé par un autre conducteur !");
        }


        boolean permisExists = conducteurRepository.findAll().stream()
                .anyMatch(c -> c.getNumeroPermis().equalsIgnoreCase(dto.numeroPermis()) && !c.getId().equals(id));
        if (permisExists) {
            throw new BusinessRuleException("Le numéro de permis est déjà utilisé par un autre conducteur !");
        }


        conducteurMapper.updateEntityFromDto(dto, conducteur);
        Conducteur updatedConducteur = conducteurRepository.save(conducteur);

        return conducteurMapper.toResponseDto(updatedConducteur);
    }

    @Override
    public void delete(Long id) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + id));
        conducteurRepository.delete(conducteur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponseDto> searchByEmailAndNom(String keyword) {
        return conducteurRepository.findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword).stream()
                .map(conducteurMapper::toResponseDto)
                .toList();
    }
}