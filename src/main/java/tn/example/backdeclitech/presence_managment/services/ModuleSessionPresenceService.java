package tn.example.backdeclitech.presence_managment.services;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tn.example.backdeclitech.presence_managment.entities.ModuleSessionPresence;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;

/**
 * Service interface for ModuleSessionPresence operations.
 */
public interface ModuleSessionPresenceService {

    /**
     * Create a new module session presence record.
     *
     * @param presence Module session presence to create
     * @return Created module session presence
     */
    ModuleSessionPresence createModuleSessionPresence(ModuleSessionPresence presence);

    /**
     * Retrieve a module session presence record by ID.
     *
     * @param id ID of the module session presence
     * @return Module session presence with the given ID
     */
    ModuleSessionPresence getModuleSessionPresenceById(Long id);
    /**
     * Update an existing module session presence record.
     *
     * @param id ID of the presence to update
     * @param updatedPresence Updated module session presence data
     * @return Updated module session presence
     */
    ModuleSessionPresence updateModuleSessionPresence(Long id, ModuleSessionPresence updatedPresence);

    /**
     * Partially update an existing module session presence record using a map of field names and values.
     *
     * @param id       ID of the presence to update
     * @param updates  Map containing field names and their new values
     * @return Updated module session presence
     */
    ModuleSessionPresence partialUpdateModuleSessionPresence(Long id, Map<String, Object> updates);

    /**
     * Delete a module session presence record by ID.
     *
     * @param id ID of the presence to delete
     */
    void deleteModuleSessionPresence(Long id);

    /**
     * Retrieve today's ModuleSessionPresence list, cached for performance.
     * 
     * @param date The date to retrieve entries for
     * @param pageable Pagination information
     * @return Page containing the ModuleSessionPresence records for the given date
     */
    Page<ModuleSessionPresence> fetchModuleSessionPresenceByDate(java.time.LocalDate date, Pageable pageable);

    /**
     * Retrieve all module session presence records.
     *
     * @param pageable Pagination information
     * @return Page containing all module session presence records
     */
    Page<ModuleSessionPresence> getAllModuleSessionPresences(Pageable pageable);


    /**
     * Retrieve all module session presence records filtered by the given request.
     *
     * @param filter   {@link GenericFilterRequest} containing the filter criteria
     * @param pageable Pagination information
     * @return Page containing the filtered module session presence records
     */
    Page<ModuleSessionPresence> filterModuleSessionPresences(
        GenericFilterRequest filter, 
        Pageable pageable
    );
}