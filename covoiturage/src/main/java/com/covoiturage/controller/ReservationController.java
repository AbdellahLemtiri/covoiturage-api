package com.covoiturage.controller;

import com.covoiturage.dto.request.ReservationRequestDto;
import com.covoiturage.dto.response.ReservationResponseDto;
import com.covoiturage.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponseDto> reserverPlaces(@Valid @RequestBody ReservationRequestDto dto) {
        ReservationResponseDto created = reservationService.reserver(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> consulterReservation(
            @PathVariable Long id,
            @RequestParam Long passagerId) {
        return ResponseEntity.ok(reservationService.consulterReservation(id, passagerId));
    }

    @GetMapping("/passager/{passagerId}")
    public ResponseEntity<List<ReservationResponseDto>> consulterMesReservations(@PathVariable Long passagerId) {
        return ResponseEntity.ok(reservationService.findByPassager(passagerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annulerReservation(
            @PathVariable Long id,
            @RequestParam Long passagerId) {
        reservationService.annulerReservation(id, passagerId);
        return ResponseEntity.noContent().build();
    }
}