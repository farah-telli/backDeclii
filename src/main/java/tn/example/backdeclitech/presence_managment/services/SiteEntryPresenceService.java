package tn.example.backdeclitech.presence_managment.services;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tn.example.backdeclitech.presence_managment.entities.SiteEntryPresence;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;

/**
 * Service interface for SiteEntryPresence operations.
 */
public interface SiteEntryPresenceService {

    /**
     * Create a new site entry presence record.
     *
     * @param presence Site entry presence to create
     * @return Created site entry presence
     */
    SiteEntryPresence createSiteEntryPresence(SiteEntryPresence presence);

    /**
     * Update an existing site entry presence record.
     *
     * @param id              ID of the presence to update
     * @param updatedPresence Updated site entry presence data
     * @return Updated site entry presence
     */
    SiteEntryPresence updateSiteEntryPresence(Long id, SiteEntryPresence updatedPresence);

    /**
     * Partially update an existing site entry presence record.
     *
     * @param id      ID of the presence to update
     * @param updates Map containing the fields to update
     * @return Updated site entry presence
     */
    SiteEntryPresence partialUpdateSiteEntryPresence(Long id, Map<String, Object> updates);

    /**
     * Delete a site entry presence record.
     *
     * @param id ID of the presence to delete
     */
    void deleteSiteEntryPresence(Long id);

    /**
     * Retrieve a site entry presence record by its ID.
     *
     * @param id ID of the presence to retrieve
     * @return Site entry presence with the specified ID
     */
    SiteEntryPresence getSiteEntryPresenceById(Long id);

    /**
     * Retrieve today's site entry presence list, cached for performance.
     *
     * @param date The date to retrieve entries for
     * @param pageable Pagination information
     * @return Page containing the site entry presence records for the given date
     */
    public Page<SiteEntryPresence> fetchDailyEntryPage(LocalDate date, Pageable pageable);

    /**
     * Retrieve site entry presence records between specific dates.
     *
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param pageable  Pagination information
     * @return Page containing the site entry presence records for the given date range
     */
    Page<SiteEntryPresence> fetchSiteEntryPresencesBetweenDates(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Retrieve all site entry presence records.
     *
     * @param pageable Pagination information
     * @return Page containing all site entry presence records
     */
    Page<SiteEntryPresence> getAllSiteEntryPresences(Pageable pageable);

    /**
     * Retrieve all site entry presence records filtered by the given request.
     *
     * @param filter   {@link GenericFilterRequest} containing the filter criteria
     * @param pageable Pagination information
     * @return Page containing the filtered site entry presence records
     */
    Page<SiteEntryPresence> filterSiteEntryPresences(
            GenericFilterRequest filter,
            Pageable pageable);

}