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
}
