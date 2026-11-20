package com.covoiturage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.covoiturage.entity.Vehicule;


public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    boolean existsByImmatriculationIgnoreCase(String immatriculation);

    List<Vehicule> findByConducteurId(Long conducteurId);
    boolean existsByConducteurIdAndId(Long conducteurId, Long id);
    boolean existsByImmatriculationIgnoreCaseAndIdNot(String immatriculation, Long id);
}