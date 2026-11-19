package com.covoiturage.controller;

import com.covoiturage.dto.request.ConducteurRequestDto;
import com.covoiturage.dto.response.ConducteurResponseDto;
import com.covoiturage.service.ConducteurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
@RequiredArgsConstructor
public class ConducteurController {

    private final ConducteurService conducteurService;

    @PostMapping
    public ResponseEntity<ConducteurResponseDto> creat(@Valid @RequestBody ConducteurRequestDto dto) {
        ConducteurResponseDto created = conducteurService.creat(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConducteurResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(conducteurService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ConducteurResponseDto>> findALl() {
        return ResponseEntity.ok(conducteurService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConducteurResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ConducteurRequestDto dto) {
        return ResponseEntity.ok(conducteurService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        conducteurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ConducteurResponseDto>> searchByEmailAndNom(@RequestParam String keyword) {
        return ResponseEntity.ok(conducteurService.searchByEmailAndNom(keyword));
    }
}