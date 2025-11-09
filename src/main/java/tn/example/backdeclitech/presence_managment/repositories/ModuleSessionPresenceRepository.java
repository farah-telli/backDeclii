package tn.example.backdeclitech.presence_managment.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tn.example.backdeclitech.presence_managment.entities.ModuleSessionPresence;

@Repository
public interface ModuleSessionPresenceRepository
                extends JpaRepository<ModuleSessionPresence, Long>, JpaSpecificationExecutor<ModuleSessionPresence> {

        /**
         * Find all module session presences.
         *
         * @param pageable Pagination information
         * @return Page of ModuleSessionPresence entities ordered by date time
         *         descending
         */
        @Query("SELECT p FROM ModuleSessionPresence p ORDER BY p.presence.reservationDate DESC")
        Page<ModuleSessionPresence> findAllModuleSessionPresences(Pageable pageable);

        /**
         * Find module session presences for a specific date.
         *
         * @param date Reservation date
         * @return List of ModuleSessionPresence for the given date
         */
        @Query("SELECT p FROM ModuleSessionPresence p WHERE p.presence.reservationDate >= :start AND p.presence.reservationDate < :end")
        Page<ModuleSessionPresence> findByPresence_ReservationDateBetween(LocalDateTime start, LocalDateTime end,
                        Pageable pageable);

}
