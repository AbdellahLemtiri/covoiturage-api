package com.covoiturage.service.impl;

import com.covoiturage.dto.request.VehiculeRequestDto;
import com.covoiturage.dto.response.VehiculeResponseDto;
import com.covoiturage.entity.Conducteur;
import com.covoiturage.entity.Vehicule;
import com.covoiturage.exception.BadRequestException;
import com.covoiturage.exception.BusinessRuleException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.VehiculeMapper;
import com.covoiturage.repository.ConducteurRepository;
import com.covoiturage.repository.TrajetRepository;
import com.covoiturage.repository.VehiculeRepository;
import com.covoiturage.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final TrajetRepository trajetRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    public VehiculeResponseDto creat(VehiculeRequestDto dto) {
        validerCapacite(dto.capacite());

        if (vehiculeRepository.existsByImmatriculationIgnoreCase(dto.immatriculation())) {
            log.warn("Ajout véhicule refusé : immatriculation déjà existante ({})", dto.immatriculation());
            throw new BusinessRuleException("L'immatriculation est déjà enregistrée");
        }

        Conducteur conducteur = conducteurRepository.findById(dto.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + dto.conducteurId()));

        Vehicule vehicule = vehiculeMapper.toEntity(dto);
        vehicule.setConducteur(conducteur);

        Vehicule saved = vehiculeRepository.save(vehicule);
        log.info("Véhicule ajouté avec succès (id={}, conducteurId={})", saved.getId(), conducteur.getId());
        return vehiculeMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponseDto findById(Long id) {
        return vehiculeMapper.toResponseDto(getVehiculeOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponseDto> findALl() {
        return vehiculeRepository.findAll().stream()
                .map(vehiculeMapper::toResponseDto)
                .toList();
    }

    @Override
    public VehiculeResponseDto update(Long id, VehiculeRequestDto dto) {
        validerCapacite(dto.capacite());
        Vehicule vehicule = getVehiculeOrThrow(id);

        boolean immatPriseParAutre = vehiculeRepository.existsByImmatriculationIgnoreCaseAndIdNot(dto.immatriculation(), id);
        if (immatPriseParAutre) {
            log.warn("Modification véhicule id={} refusée : immatriculation {} déjà utilisée", id, dto.immatriculation());
            throw new BusinessRuleException("L'immatriculation est déjà utilisée par un autre véhicule");
        }

        Conducteur conducteur = conducteurRepository.findById(dto.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + dto.conducteurId()));

        vehiculeMapper.updateEntityFromDto(dto, vehicule);
        vehicule.setConducteur(conducteur);

        Vehicule updated = vehiculeRepository.save(vehicule);
        log.info("Véhicule mis à jour avec succès (id={})", updated.getId());
        return vehiculeMapper.toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        Vehicule vehicule = getVehiculeOrThrow(id);

        if (trajetRepository.existsByVehiculeId(id)) {
            log.warn("Suppression véhicule id={} refusée : associé à au moins un trajet", id);
            throw new BusinessRuleException("Impossible de supprimer ce véhicule car il est associé à un ou plusieurs trajets");
        }

        vehiculeRepository.delete(vehicule);
        log.info("Véhicule supprimé avec succès (id={})", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponseDto> findByConducteurId(Long conducteurId) {
        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + conducteurId);
        }
        return vehiculeRepository.findByConducteurId(conducteurId).stream()
                .map(vehiculeMapper::toResponseDto)
                .toList();
    }

    private Vehicule getVehiculeOrThrow(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'identifiant : " + id));
    }

    private void validerCapacite(Integer capacite) {
        if (capacite == null || capacite < 1 || capacite > 8) {
            throw new BadRequestException("La capacité d'un véhicule doit être comprise entre 1 et 8 places passagers");
        }
    }
}