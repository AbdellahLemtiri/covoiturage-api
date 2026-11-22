package com.covoiturage.service.impl;

import com.covoiturage.dto.request.ConducteurRequestDto;
import com.covoiturage.dto.response.ConducteurResponseDto;
import com.covoiturage.entity.Conducteur;
import com.covoiturage.entity.Vehicule;
import com.covoiturage.exception.BusinessRuleException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.ConducteurMapper;
import com.covoiturage.repository.ConducteurRepository;
import com.covoiturage.service.ConducteurService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurMapper conducteurMapper;
    private final ConducteurRepository conducteurRepository;

    @Override
    public ConducteurResponseDto create(ConducteurRequestDto dto) {
        if (conducteurRepository.existsByEmailIgnoreCase(dto.email())) {
            log.warn("Ajout conducteur refusé : L'email est déjà utilisé ({})", dto.email());

            throw new BusinessRuleException("L'email est déjà utilisé !, merci de changer");
        }
        if (conducteurRepository.existsByNumeroPermisIgnoreCase(dto.numeroPermis())) {
            log.warn("Ajout conducteur refusé : Le numéro de permis est déjà utilisé ({})", dto.numeroPermis());

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


        boolean emailExists = conducteurRepository.existsByEmailIgnoreCaseAndIdNot(dto.email(), id);
        if (emailExists) {
            log.warn("L'email {} est déjà utilisé par un autre conducteu ", dto.email());
            throw new BusinessRuleException("L'email est déjà utilisé par un autre conducteur !");
        }


        boolean permisExists = conducteurRepository.existsByNumeroPermisIgnoreCaseAndIdNot(conducteur.getNumeroPermis(), id);
        if (permisExists) {
            log.warn("Le numéro de permis{} est déjà utilisé par un autre conducteur ", dto.numeroPermis());
            throw new BusinessRuleException("Le numéro de permis est déjà utilisé par un autre conducteur !");
        }


        conducteurMapper.updateEntityFromDto(dto, conducteur);
        Conducteur updatedConducteur = conducteurRepository.save(conducteur);

        log.info("Conducteur mis à jour avec succès (id={})", updatedConducteur.getId());

        return conducteurMapper.toResponseDto(updatedConducteur);
    }

    @Override
    public void delete(Long id) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + id));
        conducteurRepository.delete(conducteur);

        log.info("Conduceteur supprimé avec succès (id={})", id);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponseDto> searchByEmailAndNom(String keyword) {
        return conducteurRepository.findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword).stream()
                .map(conducteurMapper::toResponseDto)
                .toList();
    }


}