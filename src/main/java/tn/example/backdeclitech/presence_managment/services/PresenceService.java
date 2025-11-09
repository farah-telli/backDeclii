package tn.example.backdeclitech.presence_managment.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.Reservation;
import tn.example.backdeclitech.presence_managment.entities.Presence;
import tn.example.backdeclitech.presence_managment.entities.PresenceStatus;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for general presence operations.
 */
public interface PresenceService {

    /**
     * Save a presence record.
     *
     * @param presence Presence instance to be saved
     * @return Saved presence instance
     */
    Presence savePresence(Presence presence);

    /**
     * Retrieve presence record by ID.
     *
     * @param id ID of the presence to retrieve
     * @return Optional containing the presence if found, otherwise empty
     */
    Optional<Presence> findPresenceById(Long id);

    /**
     * Update a presence record.
     *
     * @param presence Updated presence instance
     * @return Updated presence instance
     */
    Presence updatePresence(Long id, Presence presence);

    /**
     * Partially update a presence record.
     *
     * @param id ID of the presence to update
     * @param updates Map containing fields to update
     * @return Updated presence instance
     */
    Presence partialUpdate(Long id, Map<String, Object> updates);

    /**
     * Delete presence record by ID.
     *
     * @param id ID of the presence to delete
     */
    void deletePresence(Long id);

    /**
     * Retrieve all presence records.
     *
     * @param pageable Pagination information
     * @return Page containing all presence records
     */
    Page<Presence> getAllPresences(Pageable pageable);

    /**
     * Filter presence records based on criteria.
     *
     * @param filter  Filtering criteria
     * @param pageable Pagination information
     * @return Page containing filtered presence records
     */
    Page<Presence> filterPresences(GenericFilterRequest filter, Pageable pageable);

    /**
     * Validate a presence record.
     *
     * @param presence Presence instance to validate
     */
    void validatePresence(Presence presence);

    /**
     * Mark a presence record with a new status.
     *
     * @param presenceId ID of the presence to update
     * @param newStatus New status to set
     */
    void markPresenceStatus(Long presenceId, PresenceStatus newStatus);

    /**
     * Mark all pending presences for a specific date as absent.
     *
     * @param date Reservation date
     */
    int markAllPendingAsAbsentForDate(LocalDate date);

    /**
     * Mark all pending presences for the current date as absent.
     * Scheduled to run daily at 9 PM.
     */
    int markAllPendingAsAbsentForDate();

    /**
     * Delete pending presences for a reservation.
     *
     * @param reservation Reservation to delete pending presences for
     */
    void deletePendingPresencesForReservation(Reservation reservation);

    /**
     * Updates the denormalized names in all presence records for a given child.
     *
     * @param child The child whose presence records need to be updated.
     */
    void updatePresenceNamesForChild(Child child);
}