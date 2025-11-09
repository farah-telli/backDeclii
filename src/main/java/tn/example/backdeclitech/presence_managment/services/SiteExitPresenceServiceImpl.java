package tn.example.backdeclitech.presence_managment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.example.backdeclitech.presence_managment.entities.SiteExitPresence;
import tn.example.backdeclitech.presence_managment.exceptions.PresenceNotFoundException;
import tn.example.backdeclitech.presence_managment.repositories.SiteExitPresenceRepository;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericSpecifications;
import tn.example.backdeclitech.presence_managment.utils.updaters.Updater;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SiteExitPresence Service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteExitPresenceServiceImpl implements SiteExitPresenceService {

    private final SiteExitPresenceRepository siteExitPresenceRepository;
    private final Updater<SiteExitPresence> updater;

    /**
     * Create a new site exit presence record.
     *
     * @param presence Site exit presence to create
     * @return Created site exit presence
     */
    @Override
    @Transactional
    public SiteExitPresence createSiteExitPresence(SiteExitPresence presence) {
        log.info("Creating site exit presence for child ID: {}, site ID: {}",
                presence.getPresence().getChild().getId(), presence.getPresence().getSite().getSpaceId());
        return siteExitPresenceRepository.save(presence);
    }

    /**
     * Update an existing site exit presence record.
     *
     * @param id              ID of the presence to update
     * @param updatedPresence Updated site exit presence data
     * @return Updated site exit presence
     * @throws PresenceNotFoundException if the presence is not found
     */
    @Override
    @Transactional
    public SiteExitPresence updateSiteExitPresence(Long id, SiteExitPresence updatedPresence) {
        log.info("Updating site exit presence with ID: {}", id);
        SiteExitPresence existing = (SiteExitPresence) siteExitPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Site exit presence not found with ID: " + id));
        existing.setExitNotes(updatedPresence.getExitNotes());
        existing.setIdVerified(updatedPresence.getIdVerified());
        existing.setAuthorizedPickup(updatedPresence.getAuthorizedPickup());
        existing.setLostItems(updatedPresence.getLostItems());
        existing.setIncidentReport(updatedPresence.getIncidentReport());
        existing.setParentSignature(updatedPresence.getParentSignature());
        return (SiteExitPresence) siteExitPresenceRepository.save(existing);
    }

    /**
     * Partially update an existing site exit presence record.
     *
     * @param id ID of the presence to update
     * @param updates Map containing the fields to update
     * @return Updated site exit presence
     */
    @Override
    @Transactional
    public SiteExitPresence partialUpdateSiteExitPresence(Long id, Map<String, Object> updates) {
        log.info("Partially updating site exit presence with ID: {}", id);
        SiteExitPresence presence = (SiteExitPresence) siteExitPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Site exit presence not found with ID: " + id));
        updater.update(updates, presence);
        return siteExitPresenceRepository.save(presence);
        
    }
    /**
     * Delete a site exit presence record.
     *
     * @param id ID of the presence to delete
     */
    @Override
    @Transactional
    public void deleteSiteExitPresence(Long id) {
        log.info("Deleting site exit presence with ID: {}", id);
        siteExitPresenceRepository.deleteById(id);
    }
    /**
     * Retrieve a site exit presence record by its ID.
     *
     * @param id ID of the presence to retrieve
     * @return Site exit presence with the specified ID
     */
    @Override
    @Transactional(readOnly = true)
    public SiteExitPresence getSiteExitPresenceById(Long id) {
        log.info("Retrieving site exit presence with ID: {}", id);
        return (SiteExitPresence) siteExitPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Site exit presence not found with ID: " + id));
    }

    /**
     * Retrieve all site exit presence records filtered by the given request.
     *
     * @param filter   {@link GenericFilterRequest} containing the filter criteria
     * @param pageable Pagination information
     * @return Page containing the filtered site exit presence records
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SiteExitPresence> filterSiteExitPresences(
            GenericFilterRequest filter,
            Pageable pageable) {
        log.debug("Filtering site exit presences with criteria: {}", filter);
        return siteExitPresenceRepository.findAll(
                GenericSpecifications.forEntity(SiteExitPresence.class, filter),
                pageable);
    }

    /**
     * Retrieve all site exit presence records.
     *
     * @param pageable Pagination information
     * @return Page containing all site exit presence records
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SiteExitPresence> getAllSiteExitPresences(Pageable pageable) {
        log.debug("Fetching all site exit presences");
        return siteExitPresenceRepository.findAllSiteExitPresences(pageable);
    }

    /**
     * Retrieve today's site exit presence page, cached for performance.
     *
     * @param date The date to retrieve exits for
     * @param pageable Pagination information
     * @return Page of SiteExitPresence for the given date
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SiteExitPresence> fetchDailyExitPage(LocalDate date, Pageable pageable) {
        log.debug("Fetching cached site exit presences page for date: {}", date);
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return siteExitPresenceRepository.findByPresence_ReservationDate(date, pageable);
    }

    /**
     * Retrieve site exit presence records between specific dates.
     *
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param pageable  Pagination information
     * @return Page of SiteExitPresence for the given date range
     */
    @Override
    @org.springframework.cache.annotation.Cacheable(value = "dailyExitPage", key = "#date + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<SiteExitPresence> fetchSiteExitPresencesBetweenDates(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching site exit presences between dates: {} and {}", startDate, endDate);
        return siteExitPresenceRepository.findByPresence_ReservationDateBetween(startDate, endDate, pageable);
    }

}
