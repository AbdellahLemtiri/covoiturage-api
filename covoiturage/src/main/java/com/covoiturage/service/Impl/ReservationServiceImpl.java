package com.covoiturage.service.impl;

import com.covoiturage.dto.request.ReservationRequestDto;
import com.covoiturage.dto.response.ReservationResponseDto;
import com.covoiturage.entity.Passager;
import com.covoiturage.entity.PointArret;
import com.covoiturage.entity.Reservation;
import com.covoiturage.entity.Trajet;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.enums.StatutTrajet;
import com.covoiturage.exception.BadRequestException;
import com.covoiturage.exception.BusinessRuleException;
import com.covoiturage.exception.ForbiddenActionException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.repository.PassagerRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TrajetRepository;
import com.covoiturage.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class ReservationServiceImpl implements ReservationService {


    private final ReservationRepository reservationRepository;
    private final PassagerRepository passagerRepository;
    private final TrajetRepository trajetRepository;
    private final ReservationMapper reservationMapper;


    @Override
    public ReservationResponseDto reserver(ReservationRequestDto dto) {

        Passager passager = passagerRepository.findById(dto.passagerId()).orElseThrow(() -> new ResourceNotFoundException("Passager introuvable avec l'id : " + dto.passagerId()));

        Trajet trajet = trajetRepository.findByIdWithDetails(dto.trajetId()).orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'id : " + dto.trajetId()));

        if (reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(passager.getId(), trajet.getId(), StatutReservation.CONFIRMEE)) {
            log.warn("Réservation refusée  passager id={} a déjà une réservation confirmée sur trajet id={}", passager.getId(), trajet.getId());
            throw new BusinessRuleException("Vous avez déjà une réservation confirmée sur ce trajet");
        }
        PointArret pointMontee = trouverPointArretParVille(trajet, dto.villeMontee(), "montée");
        PointArret pointDescente = trouverPointArretParVille(trajet, dto.villeDescente(), "descente");

        if (pointMontee.getOrdre() >= pointDescente.getOrdre()) {
            log.warn("Réservation refusée montée ({}) après ou égale à descente ({}) ", dto.villeMontee(), dto.villeDescente());
            throw new BadRequestException("Le point de montee doit etre situe avant le point de descente sur le parcours");
        }
        int placesOccupees = reservationRepository.sumPlacesConfirmeesByTrajetId(trajet.getId());
        int placesRestantes = trajet.getPlacesProposees() - placesOccupees;

        if (dto.nombrePlaces() > placesRestantes) {
            log.warn("Réservation refusée {} places demandées mais {} disponibles ", dto.nombrePlaces(), placesRestantes);
            throw new BusinessRuleException("Nombre de places disponibles insuffisant (" + placesRestantes + " restante(s))");
        }
        BigDecimal prixUnitaire = pointDescente.getPrixDepuisDepart().subtract(pointMontee.getPrixDepuisDepart());

        BigDecimal montantTotal = prixUnitaire.multiply(BigDecimal.valueOf(dto.nombrePlaces())).setScale(2, RoundingMode.HALF_UP);


        Reservation reservation = Reservation.builder().passager(passager).trajet(trajet).pointMontee(pointMontee).pointDescente(pointDescente).nombrePlaces(dto.nombrePlaces()).montantTotal(montantTotal).dateReservation(LocalDateTime.now()).statut(StatutReservation.CONFIRMEE).build();

        Reservation savedReservation = reservationRepository.save(reservation);

        int nouvellesPlacesRestantes = placesRestantes - dto.nombrePlaces();
        if (nouvellesPlacesRestantes == 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
            trajetRepository.save(trajet);
            log.info("Le trajet id={} est passé automatiquement au statut COMPLET", trajet.getId());
        }

        log.info("Réservation créée (id={}, passagerId={}, trajetId={}, segment={}->{}, montant={} DH)", savedReservation.getId(), passager.getId(), trajet.getId(), pointMontee.getVille(), pointDescente.getVille(), montantTotal);

        return reservationMapper.toResponseDto(savedReservation, nouvellesPlacesRestantes);

    }


    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDto consulterReservation(Long id, Long passagerId) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec l'id : " + id));

        if (!reservation.getPassager().getId().equals(passagerId)) {
            log.warn("Consultation réservation refusée  passager id={} sur réservation id={}", passagerId, id);
            throw new ForbiddenActionException("Vous ne pouvez consulter que vos propres réservations");
        }

        int placesRestantes = calculerPlacesRestantes(reservation.getTrajet());
        return reservationMapper.toResponseDto(reservation, placesRestantes);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findByPassager(Long passagerId) {
        if (!passagerRepository.existsById(passagerId)) {
            throw new ResourceNotFoundException("Passager introuvable avec l'id : " + passagerId);
        }

        return reservationRepository.findByPassagerIdWithDetails(passagerId).stream().map(res -> reservationMapper.toResponseDto(res, calculerPlacesRestantes(res.getTrajet()))).toList();
    }


    @Override
    public void annulerReservation(Long reservationId, Long passagerId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec l'id : " + reservationId));
        if (!reservation.getPassager().getId().equals(passagerId)) {
            log.warn("Annulation réservation refusée  passager id={} sur réservation id={}", passagerId, reservationId);
            throw new ForbiddenActionException("Vous ne pouvez annuler que vos propres réservations");
        }
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new BusinessRuleException("Cette réservation est déjà annulée");
        }
        Trajet trajet = reservation.getTrajet();

        if (LocalDateTime.now().isAfter(trajet.getDateDepart().minusHours(24))) {
            log.warn("Annulation réservation id={} refusée délai de 24h dépassé (départ={})", reservationId, trajet.getDateDepart());
            throw new BusinessRuleException("Annulation impossible : le délai de 24 heures avant le départ est dépassé");

        }

        reservation.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(reservation);

        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            trajet.setStatut(StatutTrajet.PUBLIE);
            trajetRepository.save(trajet);
            log.info("Le trajet id={} est repassé au statut PUBLIE", trajet.getId());
        }

        log.info("Réservation id={} annulée avec succès par passager id={}", reservationId, passagerId);

    }


    /// //////////utils////////////////////////

    private PointArret trouverPointArretParVille(Trajet trajet, String ville, String typePoint) {
        return trajet.getPointsArret().stream().filter(pa -> pa.getVille().equalsIgnoreCase(ville.trim())).findFirst().orElseThrow(() -> new BadRequestException("La ville de " + typePoint + " '" + ville + "' ne fait pas partie du parcours de ce trajet"));
    }

    private int calculerPlacesRestantes(Trajet trajet) {
        int placesOccupees = reservationRepository.sumPlacesConfirmeesByTrajetId(trajet.getId());
        return trajet.getPlacesProposees() - placesOccupees;
    }

}
