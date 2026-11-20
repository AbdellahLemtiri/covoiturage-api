package com.covoiturage.repository;

import com.covoiturage.entity.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNumeroPermisIgnoreCase(String numeroPermis);

    List<Conducteur> findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(String nom, String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByNumeroPermisIgnoreCaseAndIdNot(String numeroPermis, Long id);
}