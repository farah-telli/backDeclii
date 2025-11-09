package tn.example.backdeclitech.presence_managment.repositories;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tn.example.backdeclitech.presence_managment.entities.SiteEntryPresence;

@Repository
public interface SiteEntryPresenceRepository
        extends JpaRepository<SiteEntryPresence, Long>, JpaSpecificationExecutor<SiteEntryPresence> {

    /**
     * Find all site entry presences.
     *
     * @param pageable Pagination information
     * @return Page of SiteEntryPresence entities ordered by reservationDate descending
     */
    @Query("SELECT p FROM SiteEntryPresence p ORDER BY p.presence.reservationDate DESC")
    Page<SiteEntryPresence> findAllSiteEntryPresences(Pageable pageable);

    /**
     * Find site entry presences for a list of specific dates.
     *
     * @param dates List of reservation dates
     * @return Page of SiteEntryPresence for the given dates
     */
    Page<SiteEntryPresence> findByPresence_ReservationDateIn(java.util.List<LocalDate> dates, Pageable pageable);

    /**
     * Find site entry presences between specific dates.
     *
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param pageable  Pagination information
     * @return Page of SiteEntryPresence for the given date range
     */
    @Query("SELECT p FROM SiteEntryPresence p WHERE p.presence.reservationDate BETWEEN :startDate AND :endDate ORDER BY p.presence.reservationDate DESC")
    Page<SiteEntryPresence> findByPresence_ReservationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
