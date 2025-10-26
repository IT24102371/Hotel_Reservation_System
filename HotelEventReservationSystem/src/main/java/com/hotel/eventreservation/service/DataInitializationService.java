package com.hotel.eventreservation.service;

import com.hotel.eventreservation.model.Venue;
import com.hotel.eventreservation.model.VenueAvailability;
import com.hotel.eventreservation.repository.VenueRepository;
import com.hotel.eventreservation.repository.VenueAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DataInitializationService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private VenueAvailabilityRepository availabilityRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // DISABLED: Auto-creation of availability slots
        // initializeAvailabilityData();
        logger.info("DataInitializationService: Auto-creation of availability slots is DISABLED");
    }
    
    private void initializeAvailabilityData() {
        try {
            // Check if data already exists
            long existingSlots = availabilityRepository.count();
            if (existingSlots > 0) {
                logger.info("Availability data already exists ({} slots), skipping initialization", existingSlots);
                return;
            }
            
            logger.info("Initializing availability data...");
            
            // Get all venues
            List<Venue> venues = venueRepository.findAll();
            if (venues.isEmpty()) {
                logger.warn("No venues found, cannot initialize availability data");
                return;
            }
            
            // Create availability slots for the next 3 months
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusMonths(3);
            
            int totalSlots = 0;
            
            for (Venue venue : venues) {
                logger.info("Creating availability slots for venue: {}", venue.getVenueName());
                
                // Create daily availability slots for each venue
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    // Skip past dates
                    if (date.isBefore(LocalDate.now())) {
                        continue;
                    }
                    
                    // Create multiple time slots per day
                    createTimeSlotsForDate(venue, date);
                    totalSlots++;
                }
            }
            
            logger.info("Successfully initialized {} availability slots for {} venues", totalSlots, venues.size());
            
        } catch (Exception e) {
            logger.error("Error initializing availability data", e);
        }
    }
    
    private void createTimeSlotsForDate(Venue venue, LocalDate date) {
        try {
            // Morning slot: 9:00 AM - 12:00 PM
            createAvailabilitySlot(venue, date, LocalTime.of(9, 0), LocalTime.of(12, 0), "Morning slot");
            
            // Afternoon slot: 1:00 PM - 5:00 PM
            createAvailabilitySlot(venue, date, LocalTime.of(13, 0), LocalTime.of(17, 0), "Afternoon slot");
            
            // Evening slot: 6:00 PM - 10:00 PM
            createAvailabilitySlot(venue, date, LocalTime.of(18, 0), LocalTime.of(22, 0), "Evening slot");
            
            // Full day slot: 9:00 AM - 10:00 PM
            createAvailabilitySlot(venue, date, LocalTime.of(9, 0), LocalTime.of(22, 0), "Full day slot");
            
        } catch (Exception e) {
            logger.warn("Failed to create time slots for venue {} on {}: {}", venue.getVenueId(), date, e.getMessage());
        }
    }
    
    private void createAvailabilitySlot(Venue venue, LocalDate date, LocalTime startTime, LocalTime endTime, String notes) {
        try {
            // Check if slot already exists by querying all slots for this venue and date
            List<VenueAvailability> existing = availabilityRepository.findByVenueAndDateRange(
                venue.getVenueId(), date, date);
            
            boolean slotExists = existing.stream().anyMatch(slot -> 
                slot.getStartTime().equals(startTime) && slot.getEndTime().equals(endTime));
            
            if (slotExists) {
                logger.debug("Availability slot already exists for venue {} on {} at {}", 
                    venue.getVenueId(), date, startTime);
                return;
            }
            
            VenueAvailability availability = new VenueAvailability(venue, date, startTime, endTime);
            availability.setNotes(notes);
            availability.setStatus(VenueAvailability.AvailabilityStatus.AVAILABLE);
            
            availabilityRepository.save(availability);
            logger.debug("Created availability slot for venue {} on {} at {}", 
                venue.getVenueId(), date, startTime);
                
        } catch (Exception e) {
            logger.warn("Failed to create availability slot for venue {} on {}: {}", 
                venue.getVenueId(), date, e.getMessage());
        }
    }
}
