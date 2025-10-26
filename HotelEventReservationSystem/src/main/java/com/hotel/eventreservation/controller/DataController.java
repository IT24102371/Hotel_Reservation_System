package com.hotel.eventreservation.controller;

import com.hotel.eventreservation.model.Venue;
import com.hotel.eventreservation.model.VenueAvailability;
import com.hotel.eventreservation.repository.VenueRepository;
import com.hotel.eventreservation.repository.VenueAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/data")
public class DataController {
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private VenueAvailabilityRepository availabilityRepository;
    
    @GetMapping("/populate-availability")
    public String populateAvailabilityData(RedirectAttributes redirectAttributes) {
        try {
            // Check if data already exists
            long existingSlots = availabilityRepository.count();
            if (existingSlots > 0) {
                redirectAttributes.addFlashAttribute("info", 
                    "Availability data already exists (" + existingSlots + " slots)");
                return "redirect:/guest/availability";
            }
            
            // Get all venues
            List<Venue> venues = venueRepository.findAll();
            if (venues.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No venues found");
                return "redirect:/guest/availability";
            }
            
            // Create availability slots for the next 3 months
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusMonths(3);
            
            int totalSlots = 0;
            
            for (Venue venue : venues) {
                // Create daily availability slots for each venue
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    // Skip past dates
                    if (date.isBefore(LocalDate.now())) {
                        continue;
                    }
                    
                    // Create multiple time slots per day
                    createTimeSlotsForDate(venue, date);
                    totalSlots += 4; // 4 slots per day
                }
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Successfully created " + totalSlots + " availability slots for " + venues.size() + " venues");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to populate availability data: " + e.getMessage());
        }
        
        return "redirect:/guest/availability";
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
            // Log error but continue
            System.err.println("Failed to create time slots for venue " + venue.getVenueId() + " on " + date + ": " + e.getMessage());
        }
    }
    
    private void createAvailabilitySlot(Venue venue, LocalDate date, LocalTime startTime, LocalTime endTime, String notes) {
        try {
            // Check if slot already exists
            List<VenueAvailability> existing = availabilityRepository.findByVenueAndDateRange(
                venue.getVenueId(), date, date);
            
            boolean slotExists = existing.stream().anyMatch(slot -> 
                slot.getStartTime().equals(startTime) && slot.getEndTime().equals(endTime));
            
            if (slotExists) {
                return; // Skip if already exists
            }
            
            VenueAvailability availability = new VenueAvailability(venue, date, startTime, endTime);
            availability.setNotes(notes);
            availability.setStatus(VenueAvailability.AvailabilityStatus.AVAILABLE);
            
            availabilityRepository.save(availability);
            
        } catch (Exception e) {
            System.err.println("Failed to create availability slot for venue " + venue.getVenueId() + " on " + date + ": " + e.getMessage());
        }
    }
}
