package com.covoiturage.service.impl;

import com.covoiturage.dto.request.PointArretRequestDto;
import com.covoiturage.dto.request.TrajetRequestDto;
import com.covoiturage.dto.response.TrajetResponseDto;
import com.covoiturage.entity.*;
import com.covoiturage.enums.StatutReservation;
import com.covoiturage.enums.StatutTrajet;
import com.covoiturage.exception.BadRequestException;
import com.covoiturage.exception.BusinessRuleException;
import com.covoiturage.exception.ForbiddenActionException;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.mapper.TrajetMapper;
import com.covoiturage.repository.ConducteurRepository;
import com.covoiturage.repository.ReservationRepository;
import com.covoiturage.repository.TrajetRepository;
import com.covoiturage.repository.VehiculeRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private ConducteurRepository conducteurRepository;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TrajetMapper trajetMapper;
    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private TrajetServiceImpl trajetService;

    private Conducteur hamid;
    private Vehicule daciaLogan;
    private LocalDateTime departFutur;

    @BeforeEach
    void setUp() {
        hamid = Conducteur.builder()
                .id(1L)
                .nom("Alami")
                .prenom("Hamid")
                .email("hamid@test.ma")
                .numeroPermis("P-12345")
                .build();

        daciaLogan = Vehicule.builder()
                .id(10L)
                .marque("Dacia")
                .modele("Logan")
                .immatriculation("12345-A-1")
                .capacite(4)
                .conducteur(hamid)
                .build();

        departFutur = LocalDateTime.now().plusDays(3);
    }

    @Test
    @DisplayName("Cas nominal : Publication d'un trajet multi-étapes valide")
    void publierTrajet_Succes() {
        List<PointArretRequestDto> points = List.of(
                new PointArretRequestDto("Rabat", 0, departFutur, BigDecimal.ZERO),
                new PointArretRequestDto("Casablanca", 1, departFutur.plusHours(1), new BigDecimal("40.00")),
                new PointArretRequestDto("Marrakech", 2, departFutur.plusHours(4), new BigDecimal("120.00"))
        );
        TrajetRequestDto request = new TrajetRequestDto(1L, 10L, 3, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(daciaLogan));
        when(trajetMapper.toPointArretEntity(any())).thenAnswer(inv -> {
            PointArretRequestDto p = inv.getArgument(0);
            return PointArret.builder()
                    .ville(p.ville())
                    .ordre(p.ordre())
                    .heurePassage(p.heurePassage())
                    .prixDepuisDepart(p.prixDepuisDepart())
                    .build();
        });
        when(trajetRepository.save(any(Trajet.class))).thenAnswer(inv -> {
            Trajet t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });
        when(trajetMapper.toResponseDto(any(Trajet.class), eq(3), isNull()))
                .thenReturn(new TrajetResponseDto(
                        100L, 1L, "Hamid Alami", 10L, "Dacia Logan",
                        "Rabat", "Marrakech", departFutur, 3, 3, null, StatutTrajet.PUBLIE, List.of()
                ));

        TrajetResponseDto response = trajetService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.villeDepart()).isEqualTo("Rabat");
        assertThat(response.villeArrivee()).isEqualTo("Marrakech");
        assertThat(response.statut()).isEqualTo(StatutTrajet.PUBLIE);
        verify(trajetRepository, times(1)).save(any(Trajet.class));
    }

    @Test
    @DisplayName("Refus si places proposées > capacité du véhicule")
    void publierTrajet_PlacesSuperieuresCapacite_ThrowsBadRequest() {
        List<PointArretRequestDto> points = List.of(
                new PointArretRequestDto("Rabat", 0, departFutur, BigDecimal.ZERO),
                new PointArretRequestDto("Marrakech", 1, departFutur.plusHours(4), new BigDecimal("120.00"))
        );
        TrajetRequestDto request = new TrajetRequestDto(1L, 10L, 5, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(daciaLogan));

        assertThatThrownBy(() -> trajetService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("compris entre 1 et 4");

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refus si le conducteur utilise le véhicule d'un autre conducteur")
    void publierTrajet_VehiculeAutreConducteur_ThrowsForbidden() {
        Conducteur autreConducteur = Conducteur.builder().id(2L).nom("Autre").build();
        Vehicule vehiculeAutre = Vehicule.builder().id(20L).capacite(4).conducteur(autreConducteur).build();

        List<PointArretRequestDto> points = List.of(
                new PointArretRequestDto("Rabat", 0, departFutur, BigDecimal.ZERO),
                new PointArretRequestDto("Marrakech", 1, departFutur.plusHours(4), new BigDecimal("120.00"))
        );
        TrajetRequestDto request = new TrajetRequestDto(1L, 20L, 3, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(20L)).thenReturn(Optional.of(vehiculeAutre));

        assertThatThrownBy(() -> trajetService.create(request))
                .isInstanceOf(ForbiddenActionException.class);

        verify(trajetRepository, never()).save(any());
    }
}
