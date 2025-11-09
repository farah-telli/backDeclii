package tn.example.backdeclitech.presence_managment.services;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tn.example.backdeclitech.presence_managment.entities.SiteExitPresence;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;


/**
 * Service interface for SiteExitPresence operations.
 */
public interface SiteExitPresenceService {

    /**
     * Create a new site exit presence record.
     *
     * @param presence Site exit presence to create
     * @return Created site exit presence
     */
    SiteExitPresence createSiteExitPresence(SiteExitPresence presence);

    /**
     * Update an existing site exit presence record.
     *
     * @param id ID of the presence to update
     * @param updatedPresence Updated site exit presence data
     * @return Updated site exit presence
     */
    SiteExitPresence updateSiteExitPresence(Long id, SiteExitPresence updatedPresence);

    /**
     * Partially update an existing site exit presence record.
     *
     * @param id ID of the presence to update
     * @param updates Map containing the fields to update
     * @return Updated site exit presence
     */
    SiteExitPresence partialUpdateSiteExitPresence(Long id, Map<String, Object> updates);

    /**
     * Delete a site exit presence record.
     *
     * @param id ID of the presence to delete
     */
    void deleteSiteExitPresence(Long id);

    /**
     * Retrieve a site exit presence record by its ID.
     *
     * @param id ID of the presence to retrieve
     * @return Site exit presence with the specified ID
     */
    SiteExitPresence getSiteExitPresenceById(Long id);

    /**
     * Retrieve a page of site exit presence records filtered by the given date.
     *
     * @param date     Date to filter by
     * @param pageable Pagination information
     * @return Page containing the filtered site exit presence records
     */
    public Page<SiteExitPresence> fetchDailyExitPage(LocalDate date, Pageable pageable);
    
    /**
     * Retrieve a page of site exit presence records filtered by the given date range.
     *
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param pageable  Pagination information
     * @return Page containing the filtered site exit presence records
     */
    public Page<SiteExitPresence> fetchSiteExitPresencesBetweenDates(LocalDate startDate, LocalDate endDate, Pageable pageable);

    

    /**
     * Retrieve all site exit presence records.
     *
     * @param pageable Pagination information
     * @return Page containing all site exit presence records
     */
    Page<SiteExitPresence> getAllSiteExitPresences(Pageable pageable);

    /**
     * Retrieve all site exit presence records filtered by the given request.
     *
     * @param filter   {@link GenericFilterRequest} containing the filter criteria
     * @param pageable Pagination information
     * @return Page containing the filtered site exit presence records
     */
    Page<SiteExitPresence> filterSiteExitPresences(
            GenericFilterRequest filter,
            Pageable pageable);

}