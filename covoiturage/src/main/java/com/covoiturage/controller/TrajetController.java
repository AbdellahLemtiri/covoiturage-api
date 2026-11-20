package com.covoiturage.controller;

import com.covoiturage.dto.request.TrajetRequestDto;
import com.covoiturage.dto.response.ReservationResponseDto;
import com.covoiturage.dto.response.TrajetResponseDto;
import com.covoiturage.service.TrajetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping
    public ResponseEntity<TrajetResponseDto> publierTrajet(@Valid @RequestBody TrajetRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trajetService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrajetResponseDto> consulterDetailTrajet(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.findByIdWithDetails(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TrajetResponseDto>> rechercherTrajets(
            @RequestParam String depart,
            @RequestParam String arrivee,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Integer placesMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateDepart").ascending());
        return ResponseEntity.ok(trajetService.searchTrajets(depart, arrivee, date, prixMax, placesMin, pageable));
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponseDto>> consulterReservationsTrajet(
            @PathVariable Long id,
            @RequestParam Long conducteurId
    ) {
        return ResponseEntity.ok(trajetService.reservationsTrajet(id, conducteurId));
    }
}
