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
    @Transactional(readOnly = true)
    public TrajetResponseDto findByIdWithDetails(Long id) {
        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'id : " + id));

        int placesRestantes = calculerPlacesRestantes(trajet);
        return trajetMapper.toResponseDto(trajet, placesRestantes, null);
    }





    @Override
    @Transactional(readOnly = true)
    public Page<TrajetResponseDto> searchTrajets(
            String depart,
            String arrivee,
            LocalDate date,
            BigDecimal prixMax,
            Integer placesMin,
            Pageable pageable
    ) {
        int placesDemandees = (placesMin == null || placesMin < 1) ? 1 : placesMin;
        LocalDateTime debutJournee = date.atStartOfDay();
        LocalDateTime finJournee = date.atTime(LocalTime.MAX);

        Page<Trajet> pageTrajets = trajetRepository.searchTrajets(
                depart.trim(),
                arrivee.trim(),
                debutJournee,
                finJournee,
                prixMax,
                placesDemandees,
                pageable
        );

        return pageTrajets.map(trajet -> {
            int placesRestantes = calculerPlacesRestantes(trajet);
            BigDecimal prixSegment = calculerPrixSegmentDepuisPoints(trajet, depart.trim(), arrivee.trim());
            return trajetMapper.toResponseDto(trajet, placesRestantes, prixSegment);
        });
    }




    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> reservationsTrajet(Long trajetId, Long conducteurId) {
        Trajet trajet = trajetRepository.findByIdWithDetails(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'id : " + trajetId));

        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            log.warn("Consultation reservations refusee  conducteur id={} sur trajet id={}", conducteurId, trajetId);
            throw new ForbiddenActionException("Vous ne pouvez consulter que les reservations de vos propres trajets");
        }

        int placesRestantes = calculerPlacesRestantes(trajet);
        return trajet.getReservations().stream()
                .map(res -> reservationMapper.toResponseDto(res, placesRestantes))
                .toList();
    }




    @Override
    public TrajetResponseDto terminerTrajet(Long trajetId, Long conducteurId) {
        Trajet trajet = trajetRepository.findByIdWithDetails(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'id : " + trajetId));

        verifierProprietaire(trajet, conducteurId);
        verifierStatutModifiable(trajet);

        if (LocalDateTime.now().isBefore(trajet.getDateDepart())) {
            log.warn("Fin trajet id={} refusee  heure actuelle avant dateDepart ({})",
                    trajetId, trajet.getDateDepart());
            throw new BusinessRuleException("Impossible de terminer le trajet avant son heure de depart prevue");
        }

        trajet.setStatut(StatutTrajet.TERMINE);
        Trajet updated = trajetRepository.save(trajet);
        log.info("Trajet id={} marque comme TERMINE par conducteur id={}", trajetId, conducteurId);

        return trajetMapper.toResponseDto(updated, calculerPlacesRestantes(updated), null);
    }




    @Override
    public TrajetResponseDto annulerTrajet(Long trajetId, Long conducteurId) {
        Trajet trajet = trajetRepository.findByIdWithDetails(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'id : " + trajetId));

        verifierProprietaire(trajet, conducteurId);
        verifierStatutModifiable(trajet);

        trajet.setStatut(StatutTrajet.ANNULE);
        trajet.getReservations().stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .forEach(r -> r.setStatut(StatutReservation.ANNULEE));

        Trajet updated = trajetRepository.save(trajet);
        int placesRestantes = calculerPlacesRestantes(updated);
        log.info("Trajet id={} annule avec toutes ses reservations confirmees", trajetId);

        return trajetMapper.toResponseDto(updated, placesRestantes, null);
    }






    ///////////////////////////////////utils///////////////////////////////////////
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

    private void verifierProprietaire(Trajet trajet, Long conducteurId) {
        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            log.warn("Action refusee: conducteur id={} n'est pas proprietaire du trajet id={}",
                    conducteurId, trajet.getId());
            throw new ForbiddenActionException("Action refusee : vous n'etes pas le conducteur de ce trajet");
        }
    }

    private void verifierStatutModifiable(Trajet trajet) {
        if (trajet.getStatut() == StatutTrajet.TERMINE || trajet.getStatut() == StatutTrajet.ANNULE) {
            log.warn("Modification refusee (409) : trajet id={} est dejà dans un etat final ({})",
                    trajet.getId(), trajet.getStatut());
            throw new BusinessRuleException("Impossible de modifier un trajet dejà TERMINe ou ANNULE");
        }
    }

    private int calculerPlacesRestantes(Trajet trajet) {
        int placesOccupees = reservationRepository.sumPlacesConfirmeesByTrajetId(trajet.getId());
        return trajet.getPlacesProposees() - placesOccupees;
    }

    private BigDecimal calculerPrixSegmentDepuisPoints(Trajet trajet, String villeDepart, String villeArrivee) {
        PointArret pDepart = null;
        PointArret pArrivee = null;

        for (PointArret pa : trajet.getPointsArret()) {
            if (pa.getVille().equalsIgnoreCase(villeDepart)) {
                pDepart = pa;
            } else if (pa.getVille().equalsIgnoreCase(villeArrivee) && pDepart != null) {
                pArrivee = pa;
                break;
            }
        }

        if (pDepart == null || pArrivee == null) {
            return null;
        }
        return pArrivee.getPrixDepuisDepart()
                .subtract(pDepart.getPrixDepuisDepart())
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}