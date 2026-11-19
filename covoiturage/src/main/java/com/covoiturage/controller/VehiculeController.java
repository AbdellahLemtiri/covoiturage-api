package com.covoiturage.controller;

import com.covoiturage.dto.request.VehiculeRequestDto;
import com.covoiturage.dto.response.VehiculeResponseDto;
import com.covoiturage.service.VehiculeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping
    public ResponseEntity<VehiculeResponseDto> creat(@Valid @RequestBody VehiculeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculeService.creat(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<VehiculeResponseDto>> findALl() {
        return ResponseEntity.ok(vehiculeService.findALl());
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody VehiculeRequestDto dto) {
        return ResponseEntity.ok(vehiculeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehiculeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conducteur/{conducteurId}")
    public ResponseEntity<List<VehiculeResponseDto>> findByConducteurId(@PathVariable Long conducteurId) {
        return ResponseEntity.ok(vehiculeService.findByConducteurId(conducteurId));
    }
}