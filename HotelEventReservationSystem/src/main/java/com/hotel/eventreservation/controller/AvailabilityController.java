package com.hotel.eventreservation.controller;

import com.hotel.eventreservation.model.Venue;
import com.hotel.eventreservation.model.VenueAvailability;
import com.hotel.eventreservation.service.AvailabilityService;
import com.hotel.eventreservation.service.VenueService;
import com.hotel.eventreservation.dto.AvailabilityDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/availability")
public class AvailabilityController {
    
    private static final Logger logger = LoggerFactory.getLogger(AvailabilityController.class);
    
    @Autowired
    private AvailabilityService availabilityService;
    
    @Autowired
    private VenueService venueService;
    
    /**
     * Guest view - Show availability calendar (read-only)
     */
    @GetMapping("/calendar")
    public String showAvailabilityCalendar(@RequestParam(required = false) Long venueId,
                                         @RequestParam(required = false) String month,
                                         Model model) {
        LocalDate targetMonth = month != null ? LocalDate.parse(month + "-01") : LocalDate.now();
        
        Map<String, Object> calendarData = availabilityService.getCalendarData(targetMonth, venueId);
        
        model.addAttribute("calendarData", calendarData);
        model.addAttribute("selectedVenueId", venueId);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("startOfMonth", calendarData.get("startOfMonth"));
        model.addAttribute("endOfMonth", calendarData.get("endOfMonth"));
        model.addAttribute("groupedByDate", calendarData.get("groupedByDate"));
        model.addAttribute("bookingMap", calendarData.get("bookingMap"));
        
        // Get all venues for filter dropdown
        List<Venue> venues = venueService.getAllActiveVenues();
        model.addAttribute("venues", venues);
        
        return "availability/calendar";
    }
    
    /**
     * Staff view - Manage availability (CRUD operations)
     */
    @GetMapping("/manage")
    public String manageAvailability(@RequestParam(required = false) Long venueId,
                                   @RequestParam(required = false) String month,
                                   @RequestParam(required = false) String status,
                                   Model model) {
        System.out.println("AvailabilityController.manageAvailability() called with venueId=" + venueId + ", month=" + month + ", status=" + status);
        LocalDate targetMonth = month != null ? LocalDate.parse(month + "-01") : LocalDate.now();
        
        Map<String, Object> calendarData = availabilityService.getCalendarData(targetMonth, venueId);
        
        // Extract data from calendarData map and add as individual attributes
        model.addAttribute("calendarData", calendarData);
        model.addAttribute("selectedVenueId", venueId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("startOfMonth", calendarData.get("startOfMonth"));
        model.addAttribute("endOfMonth", calendarData.get("endOfMonth"));
        model.addAttribute("groupedByDate", calendarData.get("groupedByDate"));
        model.addAttribute("availabilities", calendarData.get("availabilities"));
        
        // Create calendar days list for the template
        List<LocalDate> calendarDays = new ArrayList<>();
        LocalDate startOfMonth = (LocalDate) calendarData.get("startOfMonth");
        LocalDate endOfMonth = (LocalDate) calendarData.get("endOfMonth");
        
        // Add days from start to end of month
        LocalDate currentDay = startOfMonth;
        while (!currentDay.isAfter(endOfMonth)) {
            calendarDays.add(currentDay);
            currentDay = currentDay.plusDays(1);
        }
        
        model.addAttribute("calendarDays", calendarDays);
        
        // Get all venues for filter dropdown
        List<Venue> venues = venueService.getAllActiveVenues();
        model.addAttribute("venues", venues);
        
        // Get availability summary
        Map<String, Object> summary = availabilityService.getAvailabilitySummary();
        model.addAttribute("summary", summary);
        
        return "availability/manage";
    }
    
    /**
     * Create new availability slot
     */
    @PostMapping("/create")
    public String createAvailabilitySlot(@RequestParam Long venueId,
                                        @RequestParam String date,
                                        @RequestParam String startTime,
                                        @RequestParam String endTime,
                                        @RequestParam(required = false) String notes,
                                        RedirectAttributes redirectAttributes) {
        try {
            availabilityService.createAvailabilitySlot(
                venueId, 
                LocalDate.parse(date), 
                LocalTime.parse(startTime), 
                LocalTime.parse(endTime), 
                notes
            );
            redirectAttributes.addFlashAttribute("success", "Availability slot created (BLOCKED) successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create availability slot: " + e.getMessage());
        }
        return "redirect:/availability/manage";
    }
    
    /**
     * Create multiple availability slots
     */
    @PostMapping("/create-bulk")
    public String createBulkAvailabilitySlots(@RequestParam Long venueId,
                                            @RequestParam String startDate,
                                            @RequestParam String endDate,
                                            @RequestParam String startTime,
                                            @RequestParam String endTime,
                                            @RequestParam(required = false) String notes,
                                            RedirectAttributes redirectAttributes) {
        try {
            List<VenueAvailability> slots = availabilityService.createAvailabilitySlots(
                venueId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                LocalTime.parse(startTime),
                LocalTime.parse(endTime),
                notes
            );
            redirectAttributes.addFlashAttribute("success", 
                "Created " + slots.size() + " availability slots (BLOCKED) successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create availability slots: " + e.getMessage());
        }
        return "redirect:/availability/manage";
    }
    
    /**
     * Get availability slot details for editing
     */
    @GetMapping("/details/{availabilityId}")
    @ResponseBody
    public AvailabilityDetailsDTO getAvailabilityDetails(@PathVariable Long availabilityId) {
        try {
            logger.info("Fetching availability details for ID: {}", availabilityId);
            VenueAvailability availability = availabilityService.getAvailabilityById(availabilityId);
            AvailabilityDetailsDTO dto = new AvailabilityDetailsDTO(availability);
            logger.info("Successfully created DTO for availability: {}", dto.getAvailabilityId());
            return dto;
        } catch (Exception e) {
            logger.error("Error fetching availability details for ID {}: {}", availabilityId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Update availability status
     */
    @PostMapping("/update-status/{availabilityId}")
    public String updateAvailabilityStatus(@PathVariable Long availabilityId,
                                         @RequestParam String status,
                                         @RequestParam(required = false) Long bookingId,
                                         @RequestParam(required = false) String notes,
                                         @RequestParam(required = false) String maintenanceReason,
                                         RedirectAttributes redirectAttributes) {
        try {
            VenueAvailability.AvailabilityStatus newStatus = 
                VenueAvailability.AvailabilityStatus.valueOf(status.toUpperCase());
            
            availabilityService.updateAvailabilityStatus(
                availabilityId, newStatus, bookingId, notes, maintenanceReason
            );
            redirectAttributes.addFlashAttribute("success", "Availability status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update availability: " + e.getMessage());
        }
        return "redirect:/availability/manage";
    }
    
    /**
     * Block venue for maintenance
     */
    @PostMapping("/block-maintenance")
    public String blockForMaintenance(@RequestParam Long venueId,
                                     @RequestParam String date,
                                     @RequestParam String startTime,
                                     @RequestParam String endTime,
                                     @RequestParam String maintenanceReason,
                                     @RequestParam(required = false) String notes,
                                     RedirectAttributes redirectAttributes) {
        try {
            availabilityService.blockForMaintenance(
                venueId,
                LocalDate.parse(date),
                LocalTime.parse(startTime),
                LocalTime.parse(endTime),
                maintenanceReason,
                notes
            );
            redirectAttributes.addFlashAttribute("success", "Venue blocked for maintenance successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to block venue: " + e.getMessage());
        }
        return "redirect:/availability/manage";
    }
    
    /**
     * Delete availability slot
     */
    @PostMapping("/delete/{availabilityId}")
    public String deleteAvailability(@PathVariable Long availabilityId,
                                   RedirectAttributes redirectAttributes) {
        try {
            availabilityService.deleteAvailability(availabilityId);
            redirectAttributes.addFlashAttribute("success", "Availability slot deleted successfully!");
        } catch (Exception e) {
            logger.error("Failed to delete availability {}: {}", availabilityId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete availability: " + e.getMessage());
        }
        return "redirect:/availability/manage";
    }
    
    /**
     * Bulk delete availability slots
     */
    @PostMapping("/bulk-delete")
    public String bulkDeleteAvailability(@RequestParam("availabilityIds") List<Long> availabilityIds,
                                       RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = availabilityService.bulkDeleteAvailability(availabilityIds);
            redirectAttributes.addFlashAttribute("success", 
                "Deleted " + deletedCount + " availability slots successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete availability slots: " + e.getMessage());
        }
        return "redirect:/availability/manage";
    }
    
    
    /**
     * Search availability
     */
    @GetMapping("/search")
    public String searchAvailability(@RequestParam(required = false) Long venueId,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) String status,
                                   Model model) {
        VenueAvailability.AvailabilityStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            statusEnum = VenueAvailability.AvailabilityStatus.valueOf(status.toUpperCase());
        }
        
        List<VenueAvailability> results = availabilityService.searchAvailability(
            venueId,
            startDate != null ? LocalDate.parse(startDate) : null,
            endDate != null ? LocalDate.parse(endDate) : null,
            statusEnum
        );
        
        model.addAttribute("results", results);
        model.addAttribute("searchCriteria", Map.of(
            "venueId", venueId != null ? venueId : "",
            "startDate", startDate != null ? startDate : "",
            "endDate", endDate != null ? endDate : "",
            "status", status != null ? status : ""
        ));
        
        // Get all venues for filter dropdown
        List<Venue> venues = venueService.getAllActiveVenues();
        model.addAttribute("venues", venues);
        
        return "availability/search-results";
    }
    
    /**
     * Get availability summary for dashboard
     */
    @GetMapping("/summary")
    @ResponseBody
    public Map<String, Object> getAvailabilitySummary() {
        return availabilityService.getAvailabilitySummary();
    }
    
    /**
     * Get calendar data for AJAX requests
     */
    @GetMapping("/calendar-data")
    @ResponseBody
    public Map<String, Object> getCalendarData(@RequestParam(required = false) Long venueId,
                                             @RequestParam String month) {
        LocalDate targetMonth = LocalDate.parse(month + "-01");
        return availabilityService.getCalendarData(targetMonth, venueId);
    }
    
    /**
     * Debug endpoint to check user roles
     */
    @GetMapping("/debug-roles")
    @ResponseBody
    public String debugRoles(org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            StringBuilder debug = new StringBuilder();
            debug.append("User: ").append(authentication.getName()).append("\n");
            debug.append("Authorities: ").append(authentication.getAuthorities()).append("\n");
            debug.append("Principal: ").append(authentication.getPrincipal().getClass().getSimpleName()).append("\n");
            
            // Check if it's our custom principal
            if (authentication.getPrincipal() instanceof com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) {
                com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal principal = 
                    (com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                debug.append("User ID: ").append(principal.getUser().getUserId()).append("\n");
                debug.append("User Roles: ").append(principal.getUser().getRoles().stream()
                    .map(r -> r.getRoleName()).collect(java.util.stream.Collectors.toList())).append("\n");
                debug.append("Is Active: ").append(principal.getUser().getIsActive()).append("\n");
            }
            
            return debug.toString();
        }
        return "Not authenticated";
    }
    
    /**
     * Test endpoint to verify controller is working
     */
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "AvailabilityController is working!";
    }
    
    /**
     * Test endpoint to check authentication without role requirements
     */
    @GetMapping("/auth-test")
    @ResponseBody
    public String authTest(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return "NOT AUTHENTICATED";
        }
        
        if (!authentication.isAuthenticated()) {
            return "AUTHENTICATION FAILED";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("AUTHENTICATED: ").append(authentication.getName()).append("\n");
        result.append("AUTHORITIES: ").append(authentication.getAuthorities()).append("\n");
        result.append("PRINCIPAL TYPE: ").append(authentication.getPrincipal().getClass().getSimpleName()).append("\n");
        
        return result.toString();
    }
}
