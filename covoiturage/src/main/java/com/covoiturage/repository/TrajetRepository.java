package com.covoiturage.repository;

import com.covoiturage.entity.Trajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"conducteur", "vehicule"})
    @Query("""
                SELECT DISTINCT t FROM Trajet t
                JOIN t.pointsArret p1
                JOIN t.pointsArret p2
                WHERE t.statut = com.covoiturage.enums.StatutTrajet.PUBLIE
                  AND LOWER(p1.ville) = LOWER(:depart)
                  AND LOWER(p2.ville) = LOWER(:arrivee)
                  AND p1.ordre < p2.ordre
                  AND p1.heurePassage BETWEEN :debut AND :fin
                  AND (:prixMax IS NULL OR (p2.prixDepuisDepart - p1.prixDepuisDepart) <= :prixMax)
                  AND (t.placesProposees - (
                      SELECT COALESCE(SUM(r.nombrePlaces), 0)
                      FROM Reservation r
                      WHERE r.trajet = t
                        AND r.statut = com.covoiturage.enums.StatutReservation.CONFIRMEE
                  )) >= :placesMin
                ORDER BY t.dateDepart ASC
            """)
    Page<Trajet> searchTrajets(
            @Param("depart") String depart,
            @Param("arrivee") String arrivee,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("prixMax") BigDecimal prixMax,
            @Param("placesMin") Integer placesMin,
            Pageable pageable
    );
}