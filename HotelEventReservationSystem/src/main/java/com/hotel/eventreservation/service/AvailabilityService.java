package com.hotel.eventreservation.service;

import com.hotel.eventreservation.model.Venue;
import com.hotel.eventreservation.model.VenueAvailability;
import com.hotel.eventreservation.model.Booking;
import com.hotel.eventreservation.repository.VenueAvailabilityRepository;
import com.hotel.eventreservation.repository.VenueRepository;
import com.hotel.eventreservation.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AvailabilityService {
    
    private static final Logger logger = LoggerFactory.getLogger(AvailabilityService.class);
    
    @Autowired
    private VenueAvailabilityRepository availabilityRepository;
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Create availability slots for a venue
     */
    public VenueAvailability createAvailabilitySlot(Long venueId, LocalDate date, 
                                                   LocalTime startTime, LocalTime endTime, 
                                                   String notes) {
        Optional<Venue> venueOpt = venueRepository.findById(venueId);
        if (venueOpt.isEmpty()) {
            throw new RuntimeException("Venue not found");
        }
        
        Venue venue = venueOpt.get();
        
        // Check for conflicts
        List<VenueAvailability> conflicts = availabilityRepository.findConflictingAvailability(
            venueId, date, startTime, endTime, VenueAvailability.AvailabilityStatus.AVAILABLE);
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Time slot conflicts with existing availability");
        }
        
        VenueAvailability availability = new VenueAvailability(venue, date, startTime, endTime);
        availability.setNotes(notes);
        availability.setStatus(VenueAvailability.AvailabilityStatus.BLOCKED); // Set as BLOCKED (not available)
        
        availability = availabilityRepository.save(availability);
        logger.info("Availability slot created (BLOCKED) for venue {} on {}", venueId, date);
        return availability;
    }
    
    /**
     * Create multiple availability slots for a date range
     */
    public List<VenueAvailability> createAvailabilitySlots(Long venueId, LocalDate startDate, 
                                                           LocalDate endDate, LocalTime startTime, 
                                                           LocalTime endTime, String notes) {
        List<VenueAvailability> slots = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            try {
                VenueAvailability slot = createAvailabilitySlot(venueId, date, startTime, endTime, notes);
                slots.add(slot);
            } catch (Exception e) {
                logger.warn("Failed to create slot for venue {} on {}: {}", venueId, date, e.getMessage());
            }
        }
        
        return slots;
    }
    
    /**
     * Get availability by ID
     */
    public VenueAvailability getAvailabilityById(Long availabilityId) {
        Optional<VenueAvailability> availabilityOpt = availabilityRepository.findById(availabilityId);
        if (availabilityOpt.isEmpty()) {
            throw new RuntimeException("Availability not found");
        }
        return availabilityOpt.get();
    }
    
    /**
     * Update availability status
     */
    public VenueAvailability updateAvailabilityStatus(Long availabilityId, 
                                                     VenueAvailability.AvailabilityStatus status,
                                                     Long bookingId, String notes, String maintenanceReason) {
        Optional<VenueAvailability> availabilityOpt = availabilityRepository.findById(availabilityId);
        if (availabilityOpt.isEmpty()) {
            throw new RuntimeException("Availability not found");
        }
        
        VenueAvailability availability = availabilityOpt.get();
        availability.setStatus(status);
        availability.setBookingId(bookingId);
        availability.setNotes(notes);
        availability.setMaintenanceReason(maintenanceReason);
        
        availability = availabilityRepository.save(availability);
        logger.info("Availability status updated for slot {}", availabilityId);
        return availability;
    }
    
    /**
     * Block venue for maintenance
     */
    public VenueAvailability blockForMaintenance(Long venueId, LocalDate date, 
                                                LocalTime startTime, LocalTime endTime, 
                                                String maintenanceReason, String notes) {
        Optional<Venue> venueOpt = venueRepository.findById(venueId);
        if (venueOpt.isEmpty()) {
            throw new RuntimeException("Venue not found");
        }
        
        Venue venue = venueOpt.get();
        VenueAvailability availability = new VenueAvailability(venue, date, startTime, endTime, 
                                                              VenueAvailability.AvailabilityStatus.MAINTENANCE);
        availability.setMaintenanceReason(maintenanceReason);
        availability.setNotes(notes);
        
        availability = availabilityRepository.save(availability);
        logger.info("Venue {} blocked for maintenance on {}", venueId, date);
        return availability;
    }
    
    /**
     * Get availability for a specific venue and date
     */
    public List<VenueAvailability> getVenueAvailability(Long venueId, LocalDate date) {
        return availabilityRepository.findByVenueVenueIdAndDateAndStatus(
            venueId, date, VenueAvailability.AvailabilityStatus.AVAILABLE);
    }
    
    /**
     * Get availability for a date range
     */
    public List<VenueAvailability> getVenueAvailabilityRange(Long venueId, LocalDate startDate, LocalDate endDate) {
        return availabilityRepository.findByVenueAndDateRange(venueId, startDate, endDate);
    }
    
    /**
     * Get all available venues for a specific date
     */
    public List<VenueAvailability> getAvailableVenuesForDate(LocalDate date) {
        return availabilityRepository.findByDateAndStatus(date, VenueAvailability.AvailabilityStatus.AVAILABLE);
    }
    
    /**
     * Get all venue availability for a specific date (all statuses)
     */
    public List<VenueAvailability> getAllVenueAvailabilityForDate(LocalDate date) {
        return availabilityRepository.findByDate(date);
    }
    
    /**
     * Get available venues by type for a specific date
     */
    public List<VenueAvailability> getAvailableVenuesByTypeForDate(Venue.VenueType venueType, LocalDate date) {
        return availabilityRepository.findByVenueTypeAndDateAndStatus(
            venueType.toString(), date, VenueAvailability.AvailabilityStatus.AVAILABLE);
    }
    
    /**
     * Get calendar view data for a month
     */
    public Map<String, Object> getCalendarData(LocalDate month, Long venueId) {
        LocalDate startOfMonth = month.withDayOfMonth(1);
        LocalDate endOfMonth = month.withDayOfMonth(month.lengthOfMonth());
        
        List<VenueAvailability> availabilities;
        if (venueId != null) {
            availabilities = getVenueAvailabilityRange(venueId, startOfMonth, endOfMonth);
        } else {
            availabilities = new ArrayList<>();
            for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
                // Get ALL availability slots for each date, not just available ones
                availabilities.addAll(getAllVenueAvailabilityForDate(date));
            }
        }
        
        // Filter to show only UNAVAILABLE slots (booked, maintenance, blocked)
        // This is for management view - we don't want to see available slots
        availabilities = availabilities.stream()
            .filter(availability -> availability.getStatus() != VenueAvailability.AvailabilityStatus.AVAILABLE)
            .collect(Collectors.toList());
        
        logger.info("Retrieved {} unavailable slots for month {} (filtered out available slots)", availabilities.size(), month);
        
        // Debug: Log each slot that will be displayed
        for (VenueAvailability availability : availabilities) {
            logger.info("Slot: {} - {} {} {} (Status: {})", 
                availability.getAvailabilityId(),
                availability.getDate(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getStatus());
        }
        
        // Load booking details for booked slots
        Map<Long, Booking> bookingMap = new HashMap<>();
        for (VenueAvailability availability : availabilities) {
            if (availability.getStatus() == VenueAvailability.AvailabilityStatus.BOOKED && 
                availability.getBookingId() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findById(availability.getBookingId());
                if (bookingOpt.isPresent()) {
                    bookingMap.put(availability.getAvailabilityId(), bookingOpt.get());
                }
            }
        }
        
        Map<String, Object> calendarData = new HashMap<>();
        calendarData.put("month", month);
        calendarData.put("startOfMonth", startOfMonth);
        calendarData.put("endOfMonth", endOfMonth);
        calendarData.put("availabilities", availabilities);
        calendarData.put("bookingMap", bookingMap);
        
        // Group by date for easier frontend processing
        Map<LocalDate, List<VenueAvailability>> groupedByDate = availabilities.stream()
            .collect(Collectors.groupingBy(VenueAvailability::getDate));
        calendarData.put("groupedByDate", groupedByDate);
        
        return calendarData;
    }
    
    /**
     * Get availability summary for dashboard
     */
    public Map<String, Object> getAvailabilitySummary() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusWeeks(1);
        
        Map<String, Object> summary = new HashMap<>();
        
        // Count available slots today
        long availableToday = getAvailableVenuesForDate(today).size();
        summary.put("availableToday", availableToday);
        
        // Count total slots today
        long totalSlotsToday = availabilityRepository.findByDateAndStatus(today, VenueAvailability.AvailabilityStatus.AVAILABLE).size() +
                              availabilityRepository.findByDateAndStatus(today, VenueAvailability.AvailabilityStatus.BOOKED).size() +
                              availabilityRepository.findByDateAndStatus(today, VenueAvailability.AvailabilityStatus.MAINTENANCE).size();
        summary.put("totalSlotsToday", totalSlotsToday);
        
        // Count maintenance slots
        long maintenanceSlots = availabilityRepository.findByDateAndStatus(today, VenueAvailability.AvailabilityStatus.MAINTENANCE).size();
        summary.put("maintenanceSlots", maintenanceSlots);
        
        // Get upcoming bookings
        List<VenueAvailability> upcomingBookings = new ArrayList<>();
        for (LocalDate date = today; !date.isAfter(nextWeek); date = date.plusDays(1)) {
            upcomingBookings.addAll(availabilityRepository.findByDateAndStatus(date, VenueAvailability.AvailabilityStatus.BOOKED));
        }
        summary.put("upcomingBookings", upcomingBookings.size());
        
        return summary;
    }
    
    /**
     * Delete availability slot
     */
    public void deleteAvailability(Long availabilityId) {
        Optional<VenueAvailability> availabilityOpt = availabilityRepository.findById(availabilityId);
        if (availabilityOpt.isEmpty()) {
            throw new RuntimeException("Availability not found");
        }
        
        VenueAvailability availability = availabilityOpt.get();
        
        // Only allow deletion of available or blocked slots, not booked ones
        if (availability.getStatus() == VenueAvailability.AvailabilityStatus.BOOKED) {
            throw new RuntimeException("Cannot delete booked availability slots");
        }
        
        availabilityRepository.deleteById(availabilityId);
        logger.info("Availability slot deleted: {}", availabilityId);
    }
    
    /**
     * Bulk delete availability slots
     */
    public int bulkDeleteAvailability(List<Long> availabilityIds) {
        int deletedCount = 0;
        for (Long id : availabilityIds) {
            try {
                deleteAvailability(id);
                deletedCount++;
            } catch (Exception e) {
                logger.warn("Failed to delete availability {}: {}", id, e.getMessage());
            }
        }
        return deletedCount;
    }
    
    
    /**
     * Search availability by criteria
     */
    public List<VenueAvailability> searchAvailability(Long venueId, LocalDate startDate, 
                                                     LocalDate endDate, VenueAvailability.AvailabilityStatus status) {
        if (venueId != null && startDate != null && endDate != null) {
            return availabilityRepository.findByVenueAndDateRange(venueId, startDate, endDate)
                .stream()
                .filter(av -> status == null || av.getStatus() == status)
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
