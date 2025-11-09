
package tn.example.backdeclitech.presence_managment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.Reservation;
import tn.example.backdeclitech.presence_managment.entities.Presence;
import tn.example.backdeclitech.presence_managment.entities.PresenceStatus;
import tn.example.backdeclitech.presence_managment.events.PresenceCreatedEvent;
import tn.example.backdeclitech.presence_managment.events.PresenceRemovedEvent;
import tn.example.backdeclitech.presence_managment.events.PresenceUpdatedEvent;
import tn.example.backdeclitech.presence_managment.exceptions.InvalidPresenceDataException;
import tn.example.backdeclitech.presence_managment.exceptions.PresenceNotFoundException;
import tn.example.backdeclitech.presence_managment.repositories.PresenceRepository;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericSpecifications;
import tn.example.backdeclitech.presence_managment.utils.updaters.Updater;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of Presence Service for general presence operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PresenceServiceImpl implements PresenceService {

    private final PresenceRepository presenceRepository;
    private final Updater<Presence> updater;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Save a presence record.
     *
     * @param presence Presence instance to be saved
     * @return Saved presence instance
     */
    @Override
    @Transactional
    public Presence savePresence(Presence presence) {
        log.info("Saving presence: {}", presence.getClass().getSimpleName());
        validatePresence(presence);
        presence.validatePresence();
        presence.autoPopulateFields();
        Presence saved = presenceRepository.save(presence);
        eventPublisher.publishEvent(new PresenceCreatedEvent(this, saved));
        log.info("Successfully saved presence with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Retrieve presence record by ID.
     *
     * @param id ID of the presence to retrieve
     * @return Optional containing the presence if found, otherwise empty
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Presence> findPresenceById(Long id) {
        log.debug("Finding presence by ID: {}", id);
        return presenceRepository.findById(id);
    }

    /**
     * Update a presence record.
     *
     * @param id ID of the presence to update
     * @return Updated presence instance
     */
    @Override
    @Transactional
    public Presence updatePresence(Long id, Presence presence) {
        log.info("Updating presence with ID: {}", id);
        presenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Presence not found with ID: " + id));
        validatePresence(presence);
        presence.validatePresence();
        presence.autoPopulateFields();
        Presence updated = presenceRepository.save(Presence.builder()
                .id(id)
                .child(presence.getChild())
                .parent(presence.getParent())
                .status(presence.getStatus())
                .reservationDate(presence.getReservationDate())
                .moduleSessionPresence(presence.getModuleSessionPresence())
                .siteEntryPresence(presence.getSiteEntryPresence())
                .build());
        log.info("Successfully updated presence with ID: {}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public Presence partialUpdate(Long id, Map<String, Object> updates) {
        Presence presence = presenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Presence not found with id: " + id));

        log.info("Partially updating presence with ID: {}", id);
        updater.update(updates, presence);
        presence.validatePresence();
        presence.autoPopulateFields();
        return presenceRepository.save(presence);
    }

    /**
     * Delete presence record by ID.
     *
     * @param id ID of the presence to delete
     */
    @Override
    @Transactional
    public void deletePresence(Long id) {
        log.info("Deleting presence with ID: {}", id);
        if (!presenceRepository.existsById(id)) {
            throw new PresenceNotFoundException("Presence not found with ID: " + id);
        }
        presenceRepository.deleteById(id);
        log.info("Successfully deleted presence with ID: {}", id);
    }

    /**
     * Retrieve all presence records.
     *
     * @param pageable Pagination information
     * @return Page containing all presence records
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Presence> getAllPresences(Pageable pageable) {
        log.debug("Fetching all presences");
        return presenceRepository.findAllPresences(pageable);
    }

    /**
     * Retrieve all presence records filtered by the given request.
     *
     * @param filter   {@link GenericFilterRequest} containing the filter criteria
     * @param pageable Pagination information
     * @return Page containing the filtered presence records
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Presence> filterPresences(GenericFilterRequest filter, Pageable pageable) {
        Page<Presence> filteredPresences = presenceRepository
                .findAll(GenericSpecifications.forEntity(Presence.class, filter), pageable);
        return filteredPresences;
    }

    /**
     * Validate a presence record.
     *
     * @param presence Presence instance to validate
     * @throws InvalidPresenceDataException if validation fails
     */
    @Override
    public void validatePresence(Presence presence) {
        if (presence == null) {
            throw new InvalidPresenceDataException("Presence cannot be null");
        }
        if (presence.getChild() == null) {
            throw new InvalidPresenceDataException("Child is required");
        }
    }

    /**
     * Mark a presence record with a new status.
     *
     * @param presenceId ID of the presence to update
     * @param newStatus  New status to set
     */
    @Override
    @Transactional
    public void markPresenceStatus(Long presenceId, PresenceStatus newStatus) {
        log.info("Marking presence ID: {} with status: {}", presenceId, newStatus);
        Presence presence = presenceRepository.findById(presenceId)
                .orElseThrow(() -> new PresenceNotFoundException("Presence not found with id: " + presenceId));
        presence.setStatus(newStatus);
        presenceRepository.save(presence);

        log.info("Successfully marked presence ID: {} with status: {}", presenceId, newStatus);
        eventPublisher.publishEvent(new PresenceUpdatedEvent(this, presence));
    }

    /**
     * Mark all pending presences for a specific date as absent.
     *
     * @param date Reservation date
     */
    @Override
    @Transactional
    public int markAllPendingAsAbsentForDate(LocalDate date) {
        log.info("Marking all pending presences as absent for date: {}", date);
        int updatedCount = presenceRepository.markAllPendingAsAbsentForDate(date.atStartOfDay(), PresenceStatus.PENDING, PresenceStatus.ABSENT);
        log.info("Marked {} presences as absent for date: {}", updatedCount, date);
        return updatedCount;
    }

    /**
     * Mark all pending presences for the current date as absent.
     * Scheduled to run daily at 9 PM.
     */
    @Override
    @Transactional
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 21 * * *")
    public int markAllPendingAsAbsentForDate() {
        LocalDate date = LocalDate.now();
        log.info("Scheduled task: Marking all pending presences as absent for date: {}", date);
        int updatedCount = markAllPendingAsAbsentForDate(date);
        log.info("Scheduled task completed: Marked {} pending presences as absent for date: {}", updatedCount, date);
        return updatedCount;
    }

    /**
     * Delete pending presences for a reservation.
     *
     * @param reservation Reservation to delete pending presences for
     */
    @Override
    @Transactional
    public void deletePendingPresencesForReservation(Reservation reservation) {
        log.info("Deleting pending presences for reservation ID: {}", reservation.getId());
        Child child = reservation.getChild();
        LocalDate date = reservation.getDateReservation().toLocalDate();
        List<Presence> toDelete = presenceRepository.findByReservationDateAndStatus(date, PresenceStatus.PENDING)
                .stream()
                .filter(p -> p.getChild().getId().equals(child.getId()))
                .toList();
        if (!toDelete.isEmpty()) {
            presenceRepository.deleteAll(toDelete);
            eventPublisher.publishEvent(new PresenceRemovedEvent(this, toDelete));
            log.info("Deleted {} pending presence(s) for reservation ID: {}", toDelete.size(), reservation.getId());
        }
    }

    @Override
    @Transactional
    public void updatePresenceNamesForChild(Child child) {
        if (child == null || child.getId() == null) {
            log.warn("Attempted to update presence names for a null child or child with null ID.");
            return;
        }

        String pupilName = (child.getFirstName() != null ? child.getFirstName() : "") + " "
                + (child.getLastName() != null ? child.getLastName() : "");
        String parentName = "";
        if (child.getParent() != null) {
            parentName = (child.getParent().getFirstName() != null ? child.getParent().getFirstName() : "") + " "
                    + (child.getParent().getLastName() != null ? child.getParent().getLastName() : "");
        }

        int updatedCount = presenceRepository.updatePresenceNamesForChild(child.getId(), pupilName.trim(),
                parentName.trim());
        log.info("Updated names for {} presence records for child ID: {}", updatedCount, child.getId());
    }
}