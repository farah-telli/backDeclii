package tn.example.backdeclitech.presence_managment.repositories;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tn.example.backdeclitech.presence_managment.entities.SiteExitPresence;

@Repository
public interface SiteExitPresenceRepository
        extends JpaRepository<SiteExitPresence, Long>, JpaSpecificationExecutor<SiteExitPresence> {

    /**
     * Find all site exit presences.
     *
     * @param pageable Pagination information
     * @return Page of SiteExitPresence entities ordered by reservationDate descending
     */
    @Query("SELECT p FROM SiteExitPresence p ORDER BY p.presence.reservationDate DESC")
    Page<SiteExitPresence> findAllSiteExitPresences(Pageable pageable);

    /**
     * Find site exit presences for a specific date.
     *
     * @param date Reservation date
     * @return List of SiteExitPresence for the given date
     */
    Page<SiteExitPresence> findByPresence_ReservationDate(LocalDate date, Pageable pageable);

    /**
     * Find site exit presences between specific dates.
     *
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param pageable  Pagination information
     * @return Page of SiteExitPresence for the given date range
     */
    @Query("SELECT p FROM SiteExitPresence p WHERE p.presence.reservationDate BETWEEN :startDate AND :endDate ORDER BY p.presence.reservationDate DESC")
    Page<SiteExitPresence> findByPresence_ReservationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
