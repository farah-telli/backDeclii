package tn.example.backdeclitech.presence_managment.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tn.example.backdeclitech.presence_managment.entities.Presence;
import tn.example.backdeclitech.presence_managment.entities.PresenceStatus;

/**
 * Repository interface for managing Presence entities.
 */
@Repository
public interface PresenceRepository extends JpaSpecificationExecutor<Presence>, JpaRepository<Presence, Long> {

    /**
     * Find all presences with pagination.
     *
     * @param pageable Pagination information
     * @return Page of Presence entities ordered by dateTime descending
     */
    @Query("SELECT p FROM Presence p ORDER BY p.reservationDate DESC")
    Page<Presence> findAllPresences(Pageable pageable);

    /**
     * Update the status of all presences for a specified date from an old status to a new status.
     *
     * @param date The date for which to update presence statuses.
     * @param oldStatus The current status of the presences to be updated.
     * @param newStatus The new status to set for the presences.
     * @return The number of presence records updated.
     */
    @Transactional
    @Modifying
    @Query("UPDATE Presence p SET p.status = :newStatus WHERE p.reservationDate = :date AND p.status = :oldStatus")
    int markAllPendingAsAbsentForDate(@Param("date") java.time.LocalDateTime date, @Param("oldStatus") tn.example.backdeclitech.presence_managment.entities.PresenceStatus oldStatus, @Param("newStatus") tn.example.backdeclitech.presence_managment.entities.PresenceStatus newStatus);

    /**
     * Delete all pending presence records for a specified date.
     *
     * @param date The reservation date for which to delete pending presences.
     * @param status The status of presences to be deleted.
     * @return The number of presence records deleted.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Presence p WHERE p.reservationDate = :date AND p.status = :status")
    int deletePendingPresencesForDate(@Param("date") java.time.LocalDateTime date, @Param("status") tn.example.backdeclitech.presence_managment.entities.PresenceStatus status);

    /**
     * Find presences by reservation date and status.
     *
     * @param ReservationDate Reservation date
     * @param status          Presence status
     * @param pageable        Pagination information
     * @return Page of Presence entities for the specified date and status
     */
    Page<Presence> findByReservationDateAndStatus(LocalDate ReservationDate, PresenceStatus status, Pageable pageable);

    /**
     * Find presences by reservation date and status.
     *
     * @param ReservationDate Reservation date
     * @param status          Presence status
     * @return List of Presence entities for the specified date and status
     */
    List<Presence> findByReservationDateAndStatus(LocalDate ReservationDate, PresenceStatus status);

    /**
     * Updates the pupil and parent names for all presence records of a specific child.
     *
     * @param childId    The ID of the child to update presences for.
     * @param pupilName  The new full name of the pupil.
     * @param parentName The new full name of the parent.
     * @return The number of updated records.
     */
    @Modifying
    @Query("UPDATE Presence p SET p.pupilName = :pupilName, p.parentName = :parentName WHERE p.child.id = :childId")
    int updatePresenceNamesForChild(@Param("childId") Long childId, @Param("pupilName") String pupilName, @Param("parentName") String parentName);
}
