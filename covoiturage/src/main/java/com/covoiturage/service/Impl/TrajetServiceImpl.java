package com.covoiturage.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.covoiturage.dto.request.PointArretRequestDto;
import com.covoiturage.dto.request.TrajetRequestDto;
import com.covoiturage.dto.response.ReservationResponseDto;
import com.covoiturage.dto.response.TrajetResponseDto;
import com.covoiturage.entity.Conducteur;
import com.covoiturage.entity.PointArret;
import com.covoiturage.entity.Trajet;
import com.covoiturage.entity.Vehicule;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.enums.StatutTrajet;
import com.covoiturage.exception.BadRequestException;
import com.covoiturage.exception.BusinessRuleException;
import com.covoiturage.exception.ForbiddenActionException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.mapper.TrajetMapper;
import com.covoiturage.repository.ConducteurRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TrajetRepository;
import com.covoiturage.repository.VehiculeRepository;
import com.covoiturage.service.TrajetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ReservationRepository reservationRepository;
    private final TrajetMapper trajetMapper;
    private final ReservationMapper reservationMapper;

    @Override
    public TrajetResponseDto create(TrajetRequestDto dto) {
        Conducteur conducteur = conducteurRepository.findById(dto.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'id : " + dto.conducteurId()));

        Vehicule vehicule = vehiculeRepository.findById(dto.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'id : " + dto.vehiculeId()));

        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            log.warn("Publication trajet refusee conducteur id={} tente d'utiliser le vehicule id={}",
                    conducteur.getId(), vehicule.getId());
            throw new ForbiddenActionException("Ce vehicule n'appartient pas a ce conducteur");
        }

        if (dto.placesProposees() == null || dto.placesProposees() < 1 || dto.placesProposees() > vehicule.getCapacite()) {
            log.warn("Publication trajet refusee places proposees ({}) invalides pour capacite ({})",
                    dto.placesProposees(), vehicule.getCapacite());
            throw new BadRequestException("Le nombre de places proposees doit etre compris entre 1 et " + vehicule.getCapacite());
        }

        List<PointArretRequestDto> pointsTries = dto.pointsArret().stream()
                .sorted(Comparator.comparing(PointArretRequestDto::ordre))
                .toList();

        validerPointsArret(pointsTries);

        PointArretRequestDto premierPoint = pointsTries.get(0);
        PointArretRequestDto dernierPoint = pointsTries.get(pointsTries.size() - 1);

        Trajet trajet = Trajet.builder()
                .conducteur(conducteur)
                .vehicule(vehicule)
                .villeDepart(premierPoint.ville().trim())
                .villeArrivee(dernierPoint.ville().trim())
                .dateDepart(premierPoint.heurePassage())
                .placesProposees(dto.placesProposees())
                .statut(StatutTrajet.PUBLIE)
                .build();

        for (PointArretRequestDto pDto : pointsTries) {
            PointArret point = trajetMapper.toPointArretEntity(pDto);
            point.setVille(pDto.ville().trim());
            point.setPrixDepuisDepart(pDto.prixDepuisDepart().setScale(2, RoundingMode.HALF_UP));
            trajet.addPointArret(point);
        }

        Trajet saved = trajetRepository.save(trajet);
        log.info("Trajet publie avec succès (id={}, {} -> {}, places={})",
                saved.getId(), saved.getVilleDepart(), saved.getVilleArrivee(), saved.getPlacesProposees());

        return trajetMapper.toResponseDto(saved, saved.getPlacesProposees(), null);
    }

    @Override
    public TrajetResponseDto findByIdWithDetails(Long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<TrajetResponseDto> searchTrajets(String depart, String arrivee, LocalDate date, BigDecimal prixMax, Integer placesMin, Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReservationResponseDto> reservationsTrajet(Long trajetId, Long conducteurId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TrajetResponseDto terminerTrajet(Long trajetId, Long conducteurId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TrajetResponseDto annulerTrajet(Long trajetId, Long conducteurId) {
        throw new UnsupportedOperationException();
    }

    private void validerPointsArret(List<PointArretRequestDto> points) {
        if (points.size() < 2) {
            throw new BadRequestException("Un trajet doit contenir au moins deux points d'arret");
        }

        if (points.get(0).ordre() != 0) {
            throw new BadRequestException("Le premier point d'arret doit avoir l'ordre 0");
        }

        if (!points.get(0).heurePassage().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La date et l'heure de depart doivent etre dans le futur");
        }

        if (points.get(0).prixDepuisDepart().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Le prix depuis le depart au premier point d'arret doit etre egal à 0");
        }

        Set<String> villesVues = new HashSet<>();
        for (int i = 0; i < points.size(); i++) {
            PointArretRequestDto courant = points.get(i);
            String villeNorm = courant.ville().trim().toLowerCase();

            if (!villesVues.add(villeNorm)) {
                throw new BadRequestException("La ville '" + courant.ville() + "' apparaît plusieurs fois sur le parcours");
            }

            if (i > 0) {
                PointArretRequestDto precedent = points.get(i - 1);

                if (courant.ordre() != precedent.ordre() + 1) {
                    throw new BadRequestException("Les ordres des points d'arret doivent etre consecutifs (0, 1, 2...)");
                }

                if (!courant.heurePassage().isAfter(precedent.heurePassage())) {
                    throw new BadRequestException("Les heures de passage doivent etre strictement croissantes");
                }

                if (courant.prixDepuisDepart().compareTo(precedent.prixDepuisDepart()) <= 0) {
                    throw new BadRequestException("Le prix depuis le depart doit etre strictement croissant");
                }
            }
        }
    }
}
