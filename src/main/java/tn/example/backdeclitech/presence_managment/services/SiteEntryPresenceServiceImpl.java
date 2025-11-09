package tn.example.backdeclitech.presence_managment.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.example.backdeclitech.presence_managment.entities.SiteEntryPresence;
import tn.example.backdeclitech.presence_managment.exceptions.InvalidPresenceDataException;
import tn.example.backdeclitech.presence_managment.exceptions.PresenceNotFoundException;
import tn.example.backdeclitech.presence_managment.repositories.SiteEntryPresenceRepository;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericSpecifications;
import tn.example.backdeclitech.presence_managment.utils.updaters.Updater;

/**
 * Implementation of SiteEntryPresence Service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteEntryPresenceServiceImpl implements SiteEntryPresenceService {

    private final SiteEntryPresenceRepository siteEntryPresenceRepository;
    private final Updater<SiteEntryPresence> updater;

    /**
     * Create a new site entry presence record.
     *
     * @param presence Site entry presence to create
     * @return Created site entry presence
     * @throws InvalidPresenceDataException if the child cannot enter the site
     */
    @Override
    @Transactional
    public SiteEntryPresence createSiteEntryPresence(SiteEntryPresence presence) {
        log.info("Creating site entry presence for child ID: {}, site ID: {}",
                presence.getPresence().getChild().getId(), presence.getPresence().getSite().getSpaceId());
        return siteEntryPresenceRepository.save(presence);
    }

    /**
     * Update an existing site entry presence record.
     *
     * @param id              ID of the presence to update
     * @param updatedPresence Updated site entry presence data
     * @return Updated site entry presence
     * @throws PresenceNotFoundException if the presence is not found
     */
    @Override
    @Transactional
    public SiteEntryPresence updateSiteEntryPresence(Long id, SiteEntryPresence updatedPresence) {
        log.info("Updating site entry presence with ID: {}", id);
        SiteEntryPresence existing = (SiteEntryPresence) siteEntryPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Site entry presence not found with ID: " + id));
        existing.setSupplementaryInfo(updatedPresence.getSupplementaryInfo());
        existing.setDisciplineReport(updatedPresence.getDisciplineReport());
        return siteEntryPresenceRepository.save(existing);
    }

    /**
     * Partially update an existing site entry presence record.
     *
     * @param id ID of the presence to update
     * @param updates Map containing the fields to update
     * @return Updated site entry presence
     */
    @Override
    @Transactional
    public SiteEntryPresence partialUpdateSiteEntryPresence(Long id, Map<String, Object> updates) {
        log.info("Partially updating site entry presence with ID: {}", id);
        SiteEntryPresence existing = siteEntryPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Site entry presence not found with ID: " + id));
        updater.update(updates, existing);
        return siteEntryPresenceRepository.save(existing);
    }

    /**
     * Delete a site entry presence record.
     *
     * @param id ID of the presence to delete
     */
    @Override
    @Transactional
    public void deleteSiteEntryPresence(Long id) {
        log.info("Deleting site entry presence with ID: {}", id);
        if (!siteEntryPresenceRepository.existsById(id)) {
            throw new PresenceNotFoundException("Site entry presence not found with ID: " + id);
        }
        siteEntryPresenceRepository.deleteById(id);
    }

    /**
     * Retrieve a site entry presence record by its ID.
     *
     * @param id ID of the presence to retrieve
     * @return Site entry presence with the specified ID
     */
    @Override
    @Transactional(readOnly = true)
    public SiteEntryPresence getSiteEntryPresenceById(Long id) {
        log.info("Fetching site entry presence with ID: {}", id);
        return siteEntryPresenceRepository.findById(id)
                .orElseThrow(() -> new PresenceNotFoundException("Site entry presence not found with ID: " + id));
    }

    /**
     * Retrieve all site entry presence records.
     *
     * @param pageable Pagination information
     * @return Page containing all site entry presence records
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SiteEntryPresence> getAllSiteEntryPresences(Pageable pageable) {
        log.debug("Fetching all site entry presences");
        return siteEntryPresenceRepository.findAllSiteEntryPresences(pageable);
    }

    /**
     * Retrieve today's site entry presence list, cached for performance.
     *
     * @param date The date to retrieve entries for
     * @return List of SiteEntryPresence for the given date
     */
    @Override
    @org.springframework.cache.annotation.Cacheable(value = "dailyEntryList", key = "#date")
    @Transactional(readOnly = true)
    public Page<SiteEntryPresence> fetchDailyEntryPage(LocalDate date, Pageable pageable) {
        log.debug("Fetching cached site entry presences for date: {}", date);
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        List<LocalDate> dates = List.of(date, date.plusDays(1));
        return siteEntryPresenceRepository.findByPresence_ReservationDateIn(dates, pageable);
    }

    /**
     * Retrieve site entry presence records between specific dates.
     *
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param pageable  Pagination information
     * @return Page containing the site entry presence records for the given date range
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SiteEntryPresence> fetchSiteEntryPresencesBetweenDates(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching site entry presences between dates: {} and {}", startDate, endDate);
        return siteEntryPresenceRepository.findByPresence_ReservationDateBetween(startDate, endDate, pageable);
    }

   

    /**
     * Retrieve all site entry presence records filtered by the given request.
     *
     * @param filter   {@link GenericFilterRequest} containing the filter criteria
     * @param pageable Pagination information
     * @return Page containing the filtered site entry presence records
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SiteEntryPresence> filterSiteEntryPresences(GenericFilterRequest filter, Pageable pageable) {
        log.debug("Filtering site entry presences with criteria: {}", filter);
        return siteEntryPresenceRepository.findAll(
                GenericSpecifications.forEntity(SiteEntryPresence.class, filter),
                pageable);
    }
}
