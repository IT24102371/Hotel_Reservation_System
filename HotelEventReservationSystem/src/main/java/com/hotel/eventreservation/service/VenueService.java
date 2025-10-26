package com.hotel.eventreservation.service;

import com.hotel.eventreservation.model.Venue;
import com.hotel.eventreservation.model.VenueAvailability;
import com.hotel.eventreservation.repository.VenueAvailabilityRepository;
import com.hotel.eventreservation.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VenueService {
    
    private static final Logger logger = LoggerFactory.getLogger(VenueService.class);
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private VenueAvailabilityRepository venueAvailabilityRepository;
    
    /**
     * Create a new venue
     */
    public Venue createVenue(String venueName, Venue.VenueType venueType, Integer capacity, 
                            BigDecimal hourlyRate, String description) {
        // Validate input parameters
        if (venueName == null || venueName.trim().isEmpty()) {
            throw new IllegalArgumentException("Venue name cannot be null or empty");
        }
        if (venueType == null) {
            throw new IllegalArgumentException("Venue type cannot be null");
        }
        if (capacity == null || capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hourly rate must be greater than 0");
        }
        
        try {
            Venue venue = new Venue(venueName, venueType, capacity, hourlyRate);
            venue.setDescription(description);
            
            venue = venueRepository.save(venue);
            logger.info("Venue created successfully: {}", venueName);
            return venue;
        } catch (Exception e) {
            logger.error("Error creating venue: {}", venueName, e);
            throw e;
        }
    }
    
    /**
     * Update venue information
     */
    public Venue updateVenue(Long venueId, String venueName, Integer capacity, 
                            BigDecimal hourlyRate, String description) {
        Optional<Venue> venueOpt = venueRepository.findById(venueId);
        if (venueOpt.isEmpty()) {
            throw new RuntimeException("Venue not found");
        }
        
        Venue venue = venueOpt.get();
        venue.setVenueName(venueName);
        venue.setCapacity(capacity);
        venue.setHourlyRate(hourlyRate);
        venue.setDescription(description);
        
        venue = venueRepository.save(venue);
        logger.info("Venue updated successfully: {}", venueName);
        return venue;
    }
    
    /**
     * Deactivate venue
     */
    public Venue deactivateVenue(Long venueId) {
        Optional<Venue> venueOpt = venueRepository.findById(venueId);
        if (venueOpt.isEmpty()) {
            throw new RuntimeException("Venue not found");
        }
        
        Venue venue = venueOpt.get();
        venue.setIsActive(false);
        
        venue = venueRepository.save(venue);
        logger.info("Venue deactivated: {}", venue.getVenueName());
        return venue;
    }
    
    /**
     * Activate venue
     */
    public Venue activateVenue(Long venueId) {
        Optional<Venue> venueOpt = venueRepository.findById(venueId);
        if (venueOpt.isEmpty()) {
            throw new RuntimeException("Venue not found");
        }
        
        Venue venue = venueOpt.get();
        venue.setIsActive(true);
        
        venue = venueRepository.save(venue);
        logger.info("Venue activated: {}", venue.getVenueName());
        return venue;
    }
    
    /**
     * Get all active venues
     */
    public List<Venue> getAllActiveVenues() {
        return venueRepository.findByIsActiveTrue();
    }
    
    /**
     * Get venues by type
     */
    public List<Venue> getVenuesByType(Venue.VenueType venueType) {
        return venueRepository.findByVenueTypeAndIsActiveTrue(venueType);
    }
    
    /**
     * Get venues by capacity
     */
    public List<Venue> getVenuesByCapacity(Integer minCapacity) {
        return venueRepository.findByCapacityGreaterThanEqualAndIsActiveTrue(minCapacity);
    }
    
    /**
     * Search venues by name
     */
    public List<Venue> searchVenuesByName(String name) {
        return venueRepository.findByVenueNameContainingAndIsActiveTrue(name);
    }
    
    /**
     * Get venues by price range
     */
    public List<Venue> getVenuesByPriceRange(Double maxRate) {
        return venueRepository.findByHourlyRateLessThanEqualAndIsActiveTrueOrderByHourlyRateAsc(maxRate);
    }
    
    /**
     * Find venue by ID
     */
    public Optional<Venue> findById(Long venueId) {
        return venueRepository.findById(venueId);
    }
    
    /**
     * Check venue availability for specific date and time
     */
    public List<VenueAvailability> getVenueAvailability(Long venueId, LocalDate date) {
        return venueAvailabilityRepository.findByVenueVenueIdAndDateAndStatus(venueId, date, VenueAvailability.AvailabilityStatus.AVAILABLE);
    }
    
    /**
     * Get venue availability for date range
     */
    public List<VenueAvailability> getVenueAvailabilityRange(Long venueId, LocalDate startDate, LocalDate endDate) {
        return venueAvailabilityRepository.findByVenueAndDateRange(venueId, startDate, endDate);
    }
    
    /**
     * Get all available venues for specific date
     */
    public List<VenueAvailability> getAvailableVenuesForDate(LocalDate date) {
        return venueAvailabilityRepository.findByDateAndStatus(date, VenueAvailability.AvailabilityStatus.AVAILABLE);
    }
    
    /**
     * Get available venues by type for specific date
     */
    public List<VenueAvailability> getAvailableVenuesByTypeForDate(Venue.VenueType venueType, LocalDate date) {
        return venueAvailabilityRepository.findByVenueTypeAndDateAndStatus(venueType.toString(), date, VenueAvailability.AvailabilityStatus.AVAILABLE);
    }
    
    /**
     * Create venue availability slots
     */
    public VenueAvailability createAvailabilitySlot(Long venueId, LocalDate date, 
                                                   LocalTime startTime, LocalTime endTime) {
        VenueAvailability availability = new VenueAvailability();
        Venue venue = new Venue();
        venue.setVenueId(venueId);
        availability.setVenue(venue);
        availability.setDate(date);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setIsAvailable(true);
        
        availability = venueAvailabilityRepository.save(availability);
        logger.info("Availability slot created for venue {} on {}", venueId, date);
        return availability;
    }
    
    /**
     * Update venue availability
     */
    public VenueAvailability updateAvailability(Long availabilityId, boolean isAvailable, Long bookingId) {
        Optional<VenueAvailability> availabilityOpt = venueAvailabilityRepository.findById(availabilityId);
        if (availabilityOpt.isEmpty()) {
            throw new RuntimeException("Availability not found");
        }
        
        VenueAvailability availability = availabilityOpt.get();
        availability.setIsAvailable(isAvailable);
        availability.setBookingId(bookingId);
        
        availability = venueAvailabilityRepository.save(availability);
        logger.info("Availability updated for slot {}", availabilityId);
        return availability;
    }
    
    /**
     * Delete venue availability
     */
    public void deleteAvailability(Long availabilityId) {
        venueAvailabilityRepository.deleteById(availabilityId);
        logger.info("Availability slot deleted: {}", availabilityId);
    }
}
