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
}
