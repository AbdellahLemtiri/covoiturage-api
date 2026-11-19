package com.covoiturage.controller;

import com.covoiturage.dto.request.PassagerRequestDto;
import com.covoiturage.dto.response.PassagerResponseDto;
import com.covoiturage.dto.response.ReservationResponseDto;
import com.covoiturage.service.PassagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passagers")
@RequiredArgsConstructor
public class PassagerController {

    private final PassagerService passagerService;

    @PostMapping
    public ResponseEntity<PassagerResponseDto> creat(@Valid @RequestBody PassagerRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passagerService.creat(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassagerResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(passagerService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<PassagerResponseDto>> findALl() {
        return ResponseEntity.ok(passagerService.findALl());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassagerResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PassagerRequestDto dto) {
        return ResponseEntity.ok(passagerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passagerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<PassagerResponseDto> searchByEmail(@RequestParam String email) {
        return ResponseEntity.ok(passagerService.searchByEmail(email));
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponseDto>> findReservationsByPassagerId(@PathVariable Long id) {
        return ResponseEntity.ok(passagerService.findReservationsByPassagerId(id));
    }
}