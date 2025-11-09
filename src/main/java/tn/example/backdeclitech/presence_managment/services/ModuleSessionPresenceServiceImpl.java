package tn.example.backdeclitech.presence_managment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.example.backdeclitech.presence_managment.entities.ModuleSessionPresence;
import tn.example.backdeclitech.presence_managment.exceptions.InvalidPresenceDataException;
import tn.example.backdeclitech.presence_managment.exceptions.PresenceNotFoundException;
import tn.example.backdeclitech.presence_managment.repositories.ModuleSessionPresenceRepository;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericSpecifications;
import tn.example.backdeclitech.presence_managment.utils.updaters.Updater;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ModuleSessionPresence Service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ModuleSessionPresenceServiceImpl implements ModuleSessionPresenceService {

    private final ModuleSessionPresenceRepository moduleSessionPresenceRepository;
    private final Updater<ModuleSessionPresence> updater;

    /**
     * Create a new module session presence record.
     *
     * @param presence Module session presence to create
     * @return Created module session presence
     * @throws InvalidPresenceDataException if the child cannot attend the session
     */
    @Override
    @Transactional
    public ModuleSessionPresence createModuleSessionPresence(ModuleSessionPresence presence) {
        log.info("Creating module session presence for child ID: {}, session ID: {}",
                presence.getPresence().getChild().getId(), presence.getPresence().getSession().getId());
        return moduleSessionPresenceRepository.save(presence);
    }

    /**
     * Retrieve a module session presence record by ID.
     *
     * @param id ID of the module session presence
     * @return Module session presence with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public ModuleSessionPresence getModuleSessionPresenceById(Long id) {
        log.info("Fetching module session presence with ID: {}", id);
        return moduleSessionPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Module session presence not found with ID: " + id));
    }

    /**
     * Update an existing module session presence record.
     *
     * @param id              ID of the presence to update
     * @param updatedPresence Updated module session presence data
     * @return Updated module session presence
     * @throws PresenceNotFoundException if the presence is not found
     */
    @Override
    @Transactional
    public ModuleSessionPresence updateModuleSessionPresence(Long id, ModuleSessionPresence updatedPresence) {
        log.info("Updating module session presence with ID: {}", id);
        ModuleSessionPresence existing = moduleSessionPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Module session presence not found with ID: " + id));
        existing.setParticipationNotes(updatedPresence.getParticipationNotes());
        existing.setPerformanceRating(updatedPresence.getPerformanceRating());
        existing.setBehavioralNotes(updatedPresence.getBehavioralNotes());
        existing.setActivitiesCompleted(updatedPresence.getActivitiesCompleted());
        existing.setAbsenceReason(updatedPresence.getAbsenceReason());
        return moduleSessionPresenceRepository.save(existing);
    }

    /**
     * Partially update an existing module session presence record using a map of
     * field names and values.
     *
     * @param id      ID of the presence to update
     * @param updates Map containing field names and their new values
     * @return Updated module session presence
     * @throws PresenceNotFoundException if the presence is not found
     */
    @Override
    @Transactional
    public ModuleSessionPresence partialUpdateModuleSessionPresence(Long id, Map<String, Object> updates) {
        log.info("Partially updating module session presence with ID: {}", id);
        ModuleSessionPresence existing = moduleSessionPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Module session presence not found with ID: " + id));
        updater.update(updates, existing);
        return moduleSessionPresenceRepository.save(existing);
    }

    /**
     * Delete a module session presence record by ID.
     *
     * @param id ID of the presence to delete
     * @throws PresenceNotFoundException if the presence is not found
     */
    @Override
    @Transactional
    public void deleteModuleSessionPresence(Long id) {
        log.info("Deleting module session presence with ID: {}", id);
        ModuleSessionPresence existing = moduleSessionPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Module session presence not found with ID: " + id));
        moduleSessionPresenceRepository.delete(existing);
    }

    /**
     * Retrieve all module session presence records.
     *
     * @param pageable Pagination information
     * @return Page containing all module session presence records
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ModuleSessionPresence> getAllModuleSessionPresences(Pageable pageable) {
        log.debug("Fetching all module session presences");
        return moduleSessionPresenceRepository.findAllModuleSessionPresences(pageable);
    }

    /**
     * Retrieve all module session presence records filtered by the given request.
     *
     * @param filter   {@link GenericFilterRequest} containing the filter criteria
     * @param pageable Pagination information
     * @return Page containing the filtered module session presence records
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ModuleSessionPresence> filterModuleSessionPresences(GenericFilterRequest filter, Pageable pageable) {
        log.debug("Filtering module session presences with criteria: {}", filter);
        return moduleSessionPresenceRepository.findAll(
                GenericSpecifications.forEntity(ModuleSessionPresence.class, filter),
                pageable);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "dailyEntryList", key = "#date")
    @Transactional(readOnly = true)
    public Page<ModuleSessionPresence> fetchModuleSessionPresenceByDate(LocalDate date, Pageable pageable) {
        log.debug("Fetching cached module session presences for date: {}", date);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return moduleSessionPresenceRepository.findByPresence_ReservationDateBetween(start, end, pageable);
    }
}