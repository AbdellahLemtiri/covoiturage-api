package com.covoiturage.service;

import com.covoiturage.dto.request.ReservationRequestDto;
import com.covoiturage.entity.*;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.enums.StatutTrajet;
import com.covoiturage.exception.BadRequestException;
import com.covoiturage.exception.BusinessRuleException;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.repository.PassagerRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TrajetRepository;
import com.covoiturage.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PassagerRepository passagerRepository;
    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Passager salma;
    private Passager karim;
    private Trajet trajetHamid;
    private LocalDateTime departDans3Jours;

    @BeforeEach
    void setUp() {
        salma = Passager.builder().id(1L).nom("Bennani").prenom("Salma").email("salma@test.ma").build();
        karim = Passager.builder().id(2L).nom("Idrissi").prenom("Karim").email("karim@test.ma").build();

        departDans3Jours = LocalDateTime.now().plusDays(3);

        PointArret rabat = PointArret.builder().id(1L).ville("Rabat").ordre(0)
                .heurePassage(departDans3Jours).prixDepuisDepart(BigDecimal.ZERO).build();
        PointArret casa = PointArret.builder().id(2L).ville("Casablanca").ordre(1)
                .heurePassage(departDans3Jours.plusHours(1)).prixDepuisDepart(new BigDecimal("40.00")).build();
        PointArret settat = PointArret.builder().id(3L).ville("Settat").ordre(2)
                .heurePassage(departDans3Jours.plusHours(2)).prixDepuisDepart(new BigDecimal("70.00")).build();
        PointArret marrakech = PointArret.builder().id(4L).ville("Marrakech").ordre(3)
                .heurePassage(departDans3Jours.plusHours(4)).prixDepuisDepart(new BigDecimal("120.00")).build();

        trajetHamid = Trajet.builder()
                .id(100L)
                .villeDepart("Rabat")
                .villeArrivee("Marrakech")
                .dateDepart(departDans3Jours)
                .placesProposees(3)
                .statut(StatutTrajet.PUBLIE)
                .pointsArret(List.of(rabat, casa, settat, marrakech))
                .build();
    }

    @Test
    @DisplayName("Salma réserve 1 place Settat -> Marrakech (Montant = 50.00 DH, 2 places restantes)")
    void reserverPlaces_Scenario1_Succes() {
        ReservationRequestDto dto = new ReservationRequestDto(1L, 100L, "Settat", "Marrakech", 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(salma));
        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajetHamid));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 100L, StatutReservation.CONFIRMEE))
                .thenReturn(false);
        when(reservationRepository.sumPlacesConfirmeesByTrajetId(100L)).thenReturn(0);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        reservationService.reserver(dto);

        verify(reservationRepository).save(argThat(res ->
                res.getMontantTotal().compareTo(new BigDecimal("50.00")) == 0 &&
                        res.getStatut() == StatutReservation.CONFIRMEE
        ));
        assertThat(trajetHamid.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
        verify(reservationMapper).toResponseDto(any(Reservation.class), eq(2));
    }

    @Test
    @DisplayName("Karim réserve les 2 dernières places -> Le trajet devient COMPLET")
    void reserverPlaces_Scenario3_TrajetDevientComplet() {
        ReservationRequestDto dto = new ReservationRequestDto(2L, 100L, "Casablanca", "Marrakech", 2);

        when(passagerRepository.findById(2L)).thenReturn(Optional.of(karim));
        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajetHamid));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(2L, 100L, StatutReservation.CONFIRMEE))
                .thenReturn(false);
        when(reservationRepository.sumPlacesConfirmeesByTrajetId(100L)).thenReturn(1);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        reservationService.reserver(dto);

        verify(reservationRepository).save(argThat(res ->
                res.getMontantTotal().compareTo(new BigDecimal("160.00")) == 0 &&
                        res.getStatut() == StatutReservation.CONFIRMEE
        ));
        assertThat(trajetHamid.getStatut()).isEqualTo(StatutTrajet.COMPLET);
        verify(trajetRepository).save(trajetHamid);
    }

    @Test
    @DisplayName("Refus de réserver sur un trajet COMPLET")
    void reserverPlaces_Scenario4_TrajetComplet_ThrowsConflict() {
        trajetHamid.setStatut(StatutTrajet.COMPLET);
        ReservationRequestDto dto = new ReservationRequestDto(1L, 100L, "Rabat", "Marrakech", 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(salma));
        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajetHamid));

        assertThatThrownBy(() -> reservationService.reserver(dto))
                .isInstanceOf(BusinessRuleException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refus si la montée est après la descente (Marrakech -> Settat)")
    void reserverPlaces_Scenario6_MauvaisSens_ThrowsBadRequest() {
        ReservationRequestDto dto = new ReservationRequestDto(1L, 100L, "Marrakech", "Settat", 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(salma));
        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajetHamid));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 100L, StatutReservation.CONFIRMEE))
                .thenReturn(false);

        assertThatThrownBy(() -> reservationService.reserver(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("avant le point de descente");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Annulation 3 jours avant le départ -> Réservation ANNULEE et trajet redevient PUBLIE")
    void annulerReservation_Scenario5_Succes() {
        trajetHamid.setStatut(StatutTrajet.COMPLET);
        Reservation reservationKarim = Reservation.builder()
                .id(501L)
                .passager(karim)
                .trajet(trajetHamid)
                .nombrePlaces(2)
                .statut(StatutReservation.CONFIRMEE)
                .build();

        when(reservationRepository.findById(501L)).thenReturn(Optional.of(reservationKarim));

        reservationService.annulerReservation(501L, 2L);

        assertThat(reservationKarim.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        assertThat(trajetHamid.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
        verify(trajetRepository).save(trajetHamid);
    }
}
