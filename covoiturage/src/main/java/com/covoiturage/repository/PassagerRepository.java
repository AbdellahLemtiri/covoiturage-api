package com.covoiturage.repository;

import com.covoiturage.entity.Passager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PassagerRepository extends JpaRepository<Passager,Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Passager> findByEmailIgnoreCase(String email);

}
