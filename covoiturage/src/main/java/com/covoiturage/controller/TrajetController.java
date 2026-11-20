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
}
