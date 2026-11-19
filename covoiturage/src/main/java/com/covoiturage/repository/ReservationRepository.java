package com.covoiturage.repository;

import com.covoiturage.entity.Reservation;
import com.covoiturage.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByPassagerIdAndTrajetIdAndStatut(Long passagerId, Long trajetId, StatutReservation statut);

    @Query("""
        SELECT COALESCE(SUM(r.nombrePlaces), 0)
        FROM Reservation r
        WHERE r.trajet.id = :trajetId
          AND r.statut = com.covoiturage.enums.StatutReservation.CONFIRMEE
    """)
    int sumPlacesConfirmeesByTrajetId(@Param("trajetId") Long trajetId);

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.trajet t
        JOIN FETCH r.passager
        JOIN FETCH r.pointMontee
        JOIN FETCH r.pointDescente
        WHERE r.passager.id = :passagerId
        ORDER BY r.dateReservation DESC
    """)
    List<Reservation> findByPassagerIdWithDetails(@Param("passagerId") Long passagerId);

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.trajet t
        JOIN FETCH r.passager
        JOIN FETCH r.pointMontee
        JOIN FETCH r.pointDescente
        WHERE t.conducteur.id = :conducteurId
        ORDER BY r.dateReservation DESC
    """)
    List<Reservation> findByConducteurIdWithDetails(@Param("conducteurId") Long conducteurId);
}