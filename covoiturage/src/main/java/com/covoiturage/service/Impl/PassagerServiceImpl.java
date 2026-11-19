package com.covoiturage.service.impl;

import com.covoiturage.dto.request.PassagerRequestDto;
import com.covoiturage.dto.response.PassagerResponseDto;
import com.covoiturage.dto.response.ReservationResponseDto;
import com.covoiturage.entity.Passager;
import com.covoiturage.exception.BusinessRuleException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.PassagerMapper;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.repository.PassagerRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.service.PassagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PassagerServiceImpl implements PassagerService {

    private final PassagerRepository passagerRepository;
    private final ReservationRepository reservationRepository;
    private final PassagerMapper passagerMapper;
    private final ReservationMapper reservationMapper;

    @Override
    public PassagerResponseDto creat(PassagerRequestDto dto) {
        if (passagerRepository.existsByEmailIgnoreCase(dto.email())) {
            log.warn("Création passager refusée : email déjà utilisé ({})", dto.email());
            throw new BusinessRuleException("L'email est déjà utilisé par un autre passager");
        }

        Passager saved = passagerRepository.save(passagerMapper.toEntity(dto));
        log.info("Passager créé avec succès (id={})", saved.getId());
        return passagerMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PassagerResponseDto findById(Long id) {
        Passager passager = getPassagerOrThrow(id);
        return passagerMapper.toResponseDto(passager);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponseDto> findALl() {
        return passagerRepository.findAll().stream()
                .map(passagerMapper::toResponseDto)
                .toList();
    }

    @Override
    public PassagerResponseDto update(Long id, PassagerRequestDto dto) {
        Passager passager = getPassagerOrThrow(id);

        boolean emailPrisParAutre = passagerRepository.findAll().stream()
                .anyMatch(p -> p.getEmail().equalsIgnoreCase(dto.email()) && !p.getId().equals(id));
        if (emailPrisParAutre) {
            log.warn("Modification passager id={} refusée : email {} déjà utilisé", id, dto.email());
            throw new BusinessRuleException("L'email est déjà utilisé par un autre passager");
        }

        passagerMapper.updateEntityFromDto(dto, passager);
        Passager updated = passagerRepository.save(passager);
        log.info("Passager mis à jour avec succès (id={})", updated.getId());
        return passagerMapper.toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        Passager passager = getPassagerOrThrow(id);
        passagerRepository.delete(passager);
        log.info("Passager supprimé avec succès (id={})", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PassagerResponseDto searchByEmail(String email) {
        Passager passager = passagerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Passager introuvable avec l'email : " + email));
        return passagerMapper.toResponseDto(passager);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findReservationsByPassagerId(Long passagerId) {
        getPassagerOrThrow(passagerId);
        return reservationRepository.findByPassagerIdWithDetails(passagerId).stream()
                .map(res -> {
                    int placesOccupees = reservationRepository.sumPlacesConfirmeesByTrajetId(res.getTrajet().getId());
                    int placesRestantes = res.getTrajet().getPlacesProposees() - placesOccupees;
                    return reservationMapper.toResponseDto(res, placesRestantes);
                })
                .toList();
    }

    private Passager getPassagerOrThrow(Long id) {
        return passagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passager introuvable avec l'identifiant : " + id));
    }
}