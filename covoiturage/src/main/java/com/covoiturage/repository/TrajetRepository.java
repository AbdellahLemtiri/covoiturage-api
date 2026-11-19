package com.covoiturage.repository;

import com.covoiturage.entity.Trajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    boolean existsByVehiculeId(Long vehiculeId);

    @Query("""
        SELECT t FROM Trajet t
        JOIN FETCH t.conducteur
        JOIN FETCH t.vehicule
        LEFT JOIN FETCH t.pointsArret
        WHERE t.id = :id
    """)
    Optional<Trajet> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT t FROM Trajet t
        JOIN FETCH t.conducteur
        JOIN FETCH t.vehicule
        JOIN t.pointsArret paMontee
        JOIN t.pointsArret paDescente
        WHERE t.statut = com.covoiturage.enums.StatutTrajet.PUBLIE
          AND LOWER(paMontee.ville) = LOWER(:villeDepart)
          AND LOWER(paDescente.ville) = LOWER(:villeArrivee)
          AND paMontee.ordre < paDescente.ordre
          AND paMontee.heurePassage >= :debutJournee
          AND paMontee.heurePassage <= :finJournee
          AND (:prixMax IS NULL OR (paDescente.prixDepuisDepart - paMontee.prixDepuisDepart) <= :prixMax)
          AND (
              t.placesProposees - COALESCE(
                  (SELECT SUM(r.nombrePlaces) FROM Reservation r
                   WHERE r.trajet = t
                     AND r.statut = com.covoiturage.enums.StatutReservation.CONFIRMEE), 0
              )
          ) >= :placesMin
        ORDER BY t.dateDepart ASC
    """)
    Page<Trajet> rechercherTrajets(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("debutJournee") LocalDateTime debutJournee,
            @Param("finJournee") LocalDateTime finJournee,
            @Param("prixMax") BigDecimal prixMax,
            @Param("placesMin") Integer placesMin,
            Pageable pageable
    );
}